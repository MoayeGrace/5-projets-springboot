package com.projet2.microfinance.Service;

import com.projet2.microfinance.DTO.ImputationDTO;
import com.projet2.microfinance.DTO.PaiementRequestDTO;
import com.projet2.microfinance.DTO.PaiementResponseDTO;
import com.projet2.microfinance.Entity.EcheanceEntity;
import com.projet2.microfinance.Entity.ImputationEntity;
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

    // F13 - POST /paiements : enregistre le paiement et l'impute sur les échéances, de la plus ancienne à la plus récente ;
    // sur chaque échéance : pénalités, puis intérêts, puis capital. 409 si le prêt est soldé ou si le paiement dépasse le dû
    @Transactional
    public PaiementResponseDTO creer(PaiementRequestDTO dto) {
        PretEntity pret = pretRepository.findByIdAvecVerrou(dto.getPretId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prêt " + dto.getPretId() + " introuvable"));
        verifierPaiementPossible(pret, dto.getMontant());

        PaiementEntity paiement = new PaiementEntity();
        paiement.setDatePaiement(dto.getDatePaiement());
        paiement.setMontant(dto.getMontant());
        paiement.setModeReglement(dto.getModeReglement());
        paiement.setPret(pret);

        BigDecimal reste = dto.getMontant();
        for (EcheanceEntity echeance : pret.getEcheances()) {   // déjà triées par numéro (@OrderBy)
            if (reste.signum() == 0) {
                break;
            }
            if (echeance.resteAPayer().signum() == 0) {
                continue;
            }
            ImputationEntity imputation = new ImputationEntity();
            imputation.setPaiement(paiement);
            imputation.setEcheance(echeance);

            imputation.setPenalites(reste.min(echeance.penalitesDues()));      // 1. pénalités
            reste = reste.subtract(imputation.getPenalites());
            imputation.setInterets(reste.min(echeance.interetsDus()));         // 2. intérêts
            reste = reste.subtract(imputation.getInterets());
            imputation.setCapital(reste.min(echeance.capitalDu()));            // 3. capital
            reste = reste.subtract(imputation.getCapital());

            echeance.setPenalitesPayees(echeance.getPenalitesPayees().add(imputation.getPenalites()));
            echeance.setInteretsPayes(echeance.getInteretsPayes().add(imputation.getInterets()));
            echeance.setCapitalPaye(echeance.getCapitalPaye().add(imputation.getCapital()));
            paiement.getImputations().add(imputation);
        }

        cloturerSiSolde(pret, paiement.getDatePaiement());   // F15
        return entityVersResponseDTO(paiementRepository.save(paiement));
    }

    // F15 - à la fin de chaque imputation : si tout est remboursé, le prêt passe SOLDE et la date de solde est enregistrée
    private void cloturerSiSolde(PretEntity pret, LocalDate datePaiement) {
        if (pret.estEntierementRembourse()) {
            pret.setStatut(StatutPret.SOLDE);
            pret.setDateSolde(datePaiement);
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
        paiement.setDatePaiement(dto.getDatePaiement());
        paiement.setModeReglement(dto.getModeReglement());
        return entityVersResponseDTO(paiementRepository.save(paiement));
    }

    // F13 - DELETE /paiements/{id} : annule l'imputation (les échéances redeviennent dues), puis supprime le paiement
    @Transactional
    public void supprimer(Long id) {
        PaiementEntity paiement = trouverOu404(paiementRepository, id, "Paiement");
        for (ImputationEntity imputation : paiement.getImputations()) {
            EcheanceEntity echeance = imputation.getEcheance();
            echeance.setPenalitesPayees(echeance.getPenalitesPayees().subtract(imputation.getPenalites()));
            echeance.setInteretsPayes(echeance.getInteretsPayes().subtract(imputation.getInterets()));
            echeance.setCapitalPaye(echeance.getCapitalPaye().subtract(imputation.getCapital()));
        }
        // F15 - un prêt soldé qui redevient dû repasse EN_COURS
        PretEntity pret = paiement.getPret();
        if (pret.getStatut() == StatutPret.SOLDE && !pret.estEntierementRembourse()) {
            pret.setStatut(StatutPret.EN_COURS);
            pret.setDateSolde(null);
        }
        paiementRepository.delete(paiement);
    }

    // Entity -> DTO (avec le détail de l'imputation)
    private PaiementResponseDTO entityVersResponseDTO(PaiementEntity paiement) {
        PaiementResponseDTO dto = new PaiementResponseDTO();
        dto.setId(paiement.getId());
        dto.setDatePaiement(paiement.getDatePaiement());
        dto.setMontant(paiement.getMontant());
        dto.setModeReglement(paiement.getModeReglement());
        dto.setPretId(paiement.getPret().getId());
        dto.setImputations(paiement.getImputations().stream().map(this::imputationVersDTO).toList());   // F13
        return dto;
    }

    // F13 - Entity -> DTO pour une ligne d'imputation
    private ImputationDTO imputationVersDTO(ImputationEntity imputation) {
        ImputationDTO dto = new ImputationDTO();
        dto.setEcheanceId(imputation.getEcheance().getId());
        dto.setEcheanceNumero(imputation.getEcheance().getNumero());
        dto.setPenalites(imputation.getPenalites());
        dto.setInterets(imputation.getInterets());
        dto.setCapital(imputation.getCapital());
        return dto;
    }
}
