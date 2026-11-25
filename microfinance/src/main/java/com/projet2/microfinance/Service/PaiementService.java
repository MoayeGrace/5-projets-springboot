package com.projet2.microfinance.Service;

import com.projet2.microfinance.DTO.ImputationDTO;
import com.projet2.microfinance.DTO.PaiementRequestDTO;
import com.projet2.microfinance.DTO.PaiementResponseDTO;
import com.projet2.microfinance.Entity.EcheanceEntity;
import com.projet2.microfinance.Entity.PaiementEntity;
import com.projet2.microfinance.Entity.PretEntity;
import com.projet2.microfinance.Entity.StatutPret;
import com.projet2.microfinance.Repository.PaiementRepository;
import com.projet2.microfinance.Repository.PretRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.projet2.microfinance.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final PretRepository pretRepository;

    // GET /paiements : récupère l'historique de tous les paiements
    public List<PaiementResponseDTO> lister() {
        return paiementRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /paiements/{id} : récupère un paiement par son id (404 s'il n'existe pas)
    public PaiementResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(paiementRepository, id, "Paiement"));
    }

    // F9 - GET /prets/{id}/paiements : paiements du prêt du plus récent au plus ancien (404 si le prêt n'existe pas)
    public List<PaiementResponseDTO> listerParPret(Long pretId) {
        trouverOu404(pretRepository, pretId, "Prêt");
        return paiementRepository.findByPretIdOrderByDatePaiementDescIdDesc(pretId)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // F13 - POST /paiements : enregistre le paiement et l'impute sur les échéances ; renvoie le détail échéance par échéance.
    // 409 si le prêt est soldé ou si le paiement dépasse le dû
    @Transactional
    public PaiementResponseDTO creer(PaiementRequestDTO dto) {
        PretEntity pret = pretRepository.findByIdAvecVerrou(dto.getPretId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prêt " + dto.getPretId() + " introuvable"));
        verifierDate(pret, dto.getDatePaiement());
        verifierPaiementPossible(pret, dto.getMontant());

        PaiementEntity paiement = new PaiementEntity();
        paiement.setDatePaiement(dto.getDatePaiement());
        paiement.setMontant(dto.getMontant());
        paiement.setModeReglement(dto.getModeReglement());
        paiement.setPret(pret);
        List<ImputationDTO> detail = imputer(pret, paiement);

        cloturerSiSolde(pret, paiement.getDatePaiement());   // F15
        PaiementResponseDTO reponse = entityVersResponseDTO(paiementRepository.save(paiement));
        reponse.setImputations(detail);
        return reponse;
    }

    // F13 - répartit le montant sur les échéances, de la plus ancienne à la plus récente ; sur chacune : pénalités, puis
    // intérêts, puis capital. Les parts sont cumulées sur le paiement, le détail par échéance est renvoyé
    private List<ImputationDTO> imputer(PretEntity pret, PaiementEntity paiement) {
        List<ImputationDTO> detail = new ArrayList<>();
        paiement.setPartPenalites(BigDecimal.ZERO);
        paiement.setPartInterets(BigDecimal.ZERO);
        paiement.setPartCapital(BigDecimal.ZERO);
        BigDecimal reste = paiement.getMontant();
        for (EcheanceEntity echeance : pret.getEcheances()) {   // déjà triées par date (@OrderBy)
            if (reste.signum() == 0) {
                break;
            }
            if (echeance.resteAPayer().signum() == 0) {
                continue;
            }
            BigDecimal penalites = reste.min(echeance.penalitesDues());     // 1. pénalités
            reste = reste.subtract(penalites);
            BigDecimal interets = reste.min(echeance.interetsDus());        // 2. intérêts
            reste = reste.subtract(interets);
            BigDecimal capital = reste.min(echeance.capitalDu());           // 3. capital
            reste = reste.subtract(capital);

            echeance.setPenalitesPaye(echeance.getPenalitesPaye().add(penalites));
            echeance.setInteretsPaye(echeance.getInteretsPaye().add(interets));
            echeance.setCapitalPaye(echeance.getCapitalPaye().add(capital));
            paiement.setPartPenalites(paiement.getPartPenalites().add(penalites));
            paiement.setPartInterets(paiement.getPartInterets().add(interets));
            paiement.setPartCapital(paiement.getPartCapital().add(capital));

            ImputationDTO ligne = new ImputationDTO();
            ligne.setEcheanceId(echeance.getId());
            ligne.setEcheanceNumero(echeance.numero());
            ligne.setPenalites(penalites);
            ligne.setInterets(interets);
            ligne.setCapital(capital);
            detail.add(ligne);
        }
        return detail;
    }

    // F15 - à la fin de chaque imputation : si tout est remboursé, le prêt passe SOLDE et la date de solde est enregistrée
    private void cloturerSiSolde(PretEntity pret, LocalDate datePaiement) {
        if (pret.estEntierementRembourse()) {
            pret.setStatut(StatutPret.SOLDE);
            pret.setDateSolde(datePaiement);
        }
    }

    // F13 - un paiement ne peut pas être daté dans le futur ni avant l'accord du prêt (400)
    private void verifierDate(PretEntity pret, LocalDate datePaiement) {
        if (datePaiement.isAfter(LocalDate.now()) || datePaiement.isBefore(pret.getDateAccord())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La date du paiement doit être comprise entre l'accord du prêt (" + pret.getDateAccord() + ") et aujourd'hui");
        }
    }

    // F13 - refuse un paiement sur un prêt soldé, sans échéancier, ou supérieur à ce qui reste dû (409)
    private void verifierPaiementPossible(PretEntity pret, BigDecimal montant) {
        if (pret.getStatut() == StatutPret.SOLDE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le prêt " + pret.getId() + " est soldé : il n'accepte plus de paiement");
        }
        if (pret.getEcheances().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'échéancier du prêt " + pret.getId() + " n'est pas généré");
        }
        BigDecimal totalDu = pret.getEcheances().stream()
                .map(EcheanceEntity::resteAPayer)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (montant.compareTo(totalDu) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le paiement (" + montant + ") dépasse le reste dû sur le prêt (" + totalDu + ")");
        }
    }

    // F13 - PUT /paiements/{id} : seuls la date et le mode de règlement se corrigent ; un paiement imputé ne change
    // ni de montant ni de prêt (409 : le supprimer puis le ressaisir)
    public PaiementResponseDTO modifier(Long id, PaiementRequestDTO dto) {
        PaiementEntity paiement = trouverOu404(paiementRepository, id, "Paiement");
        if (paiement.getMontant().compareTo(dto.getMontant()) != 0 || !paiement.getPret().getId().equals(dto.getPretId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le montant et le prêt d'un paiement imputé ne se modifient pas : supprimez-le puis ressaisissez-le");
        }
        verifierDate(paiement.getPret(), dto.getDatePaiement());
        paiement.setDatePaiement(dto.getDatePaiement());
        paiement.setModeReglement(dto.getModeReglement());
        return entityVersResponseDTO(paiementRepository.save(paiement));
    }

    // F13 - DELETE /paiements/{id} : supprime le paiement, remet les échéances à zéro et réimpute les paiements restants
    // dans l'ordre chronologique (ce que couvrait le paiement supprimé redevient dû)
    @Transactional
    public void supprimer(Long id) {
        PaiementEntity paiement = trouverOu404(paiementRepository, id, "Paiement");
        PretEntity pret = paiement.getPret();
        pret.getPaiements().remove(paiement);
        paiementRepository.delete(paiement);

        for (EcheanceEntity echeance : pret.getEcheances()) {
            echeance.setPenalitesPaye(BigDecimal.ZERO);
            echeance.setInteretsPaye(BigDecimal.ZERO);
            echeance.setCapitalPaye(BigDecimal.ZERO);
        }
        pret.getPaiements().stream()
                .sorted(Comparator.comparing(PaiementEntity::getDatePaiement).thenComparing(PaiementEntity::getId))
                .forEach(restant -> imputer(pret, restant));

        // F15 - un prêt soldé qui redevient dû repasse EN_COURS
        if (pret.getStatut() == StatutPret.SOLDE && !pret.estEntierementRembourse()) {
            pret.setStatut(StatutPret.EN_COURS);
            pret.setDateSolde(null);
        }
    }

    // Entity -> DTO (avec l'imputation retenue)
    private PaiementResponseDTO entityVersResponseDTO(PaiementEntity paiement) {
        PaiementResponseDTO dto = new PaiementResponseDTO();
        dto.setId(paiement.getId());
        dto.setDatePaiement(paiement.getDatePaiement());
        dto.setMontant(paiement.getMontant());
        dto.setModeReglement(paiement.getModeReglement());
        dto.setPretId(paiement.getPret().getId());
        dto.setPartPenalites(paiement.getPartPenalites());   // F9 / F13
        dto.setPartInterets(paiement.getPartInterets());
        dto.setPartCapital(paiement.getPartCapital());
        return dto;
    }
}
