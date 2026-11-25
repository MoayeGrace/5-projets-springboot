package com.projet2.microfinance.Service;

import com.projet2.microfinance.DTO.EcheanceRequestDTO;
import com.projet2.microfinance.DTO.EcheanceResponseDTO;
import com.projet2.microfinance.DTO.EcheancierDTO;
import com.projet2.microfinance.DTO.PenalitesDTO;
import com.projet2.microfinance.Entity.EcheanceEntity;
import com.projet2.microfinance.Entity.PretEntity;
import com.projet2.microfinance.Entity.StatutPret;
import com.projet2.microfinance.Repository.EcheanceRepository;
import com.projet2.microfinance.Repository.PretRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.projet2.microfinance.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class EcheanceService {

    private final EcheanceRepository echeanceRepository;
    private final PretRepository pretRepository;

    // GET /echeances : récupère toutes les échéances
    public List<EcheanceResponseDTO> lister() {
        return echeanceRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /echeances/{id} : récupère une échéance par son id (404 si elle n'existe pas)
    public EcheanceResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(echeanceRepository, id, "Échéance"));
    }

    // F8 - GET /prets/{id}/echeances : échéancier du prêt trié par date (404 si le prêt n'existe pas, "non généré" s'il est vide)
    public EcheancierDTO echeancierDuPret(Long pretId) {
        trouverOu404(pretRepository, pretId, "Prêt");
        List<EcheanceResponseDTO> echeances = echeanceRepository.findByPretIdOrderByDateEcheanceAsc(pretId)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();

        EcheancierDTO dto = new EcheancierDTO();
        dto.setPretId(pretId);
        dto.setGenere(!echeances.isEmpty());
        dto.setMessage(echeances.isEmpty() ? "L'échéancier de ce prêt n'est pas encore généré" : null);
        dto.setEcheances(echeances);
        return dto;
    }

    // F11 - POST /prets/{id}/echeancier : une échéance par mois à capital constant, intérêts = taux mensuel × capital restant dû ;
    // la dernière ajuste le capital pour que la somme égale exactement le montant. 409 si déjà généré, 400 si le prêt n'est pas ACCORDE
    @Transactional
    public EcheancierDTO genererEcheancier(Long pretId) {
        PretEntity pret = trouverOu404(pretRepository, pretId, "Prêt");
        if (!pret.getEcheances().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'échéancier de ce prêt existe déjà");
        }
        if (pret.getStatut() != StatutPret.ACCORDE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'échéancier ne peut être généré que pour un prêt au statut ACCORDE (statut actuel : " + pret.getStatut() + ")");
        }

        int duree = pret.getDureeMois();
        BigDecimal tauxMensuel = pret.getTaux().divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal capitalConstant = pret.getMontant().divide(BigDecimal.valueOf(duree), 2, RoundingMode.HALF_UP);
        BigDecimal capitalRestant = pret.getMontant();

        for (int numero = 1; numero <= duree; numero++) {
            BigDecimal interets = capitalRestant.multiply(tauxMensuel).setScale(2, RoundingMode.HALF_UP);
            // F11 - dernière échéance : on rembourse tout le capital restant (absorbe les écarts d'arrondi)
            BigDecimal capital = (numero == duree)
                    ? capitalRestant
                    : capitalConstant.min(capitalRestant);

            EcheanceEntity echeance = new EcheanceEntity();
            echeance.setDateEcheance(pret.getDateAccord().plusMonths(numero));
            echeance.setCapital(capital);
            echeance.setInterets(interets);
            echeance.setMontantTotal(capital.add(interets));
            echeance.setPret(pret);
            pret.getEcheances().add(echeanceRepository.save(echeance));

            capitalRestant = capitalRestant.subtract(capital);
        }

        pret.setStatut(StatutPret.EN_COURS);   // F11 - le prêt passe EN_COURS, ses paramètres sont désormais figés
        return echeancierDuPret(pretId);
    }

    // F14 - pénalité par jour de retard : 0,1 % du montant dû
    private static final BigDecimal TAUX_PENALITE_JOUR = new BigDecimal("0.001");

    // F14 - POST /prets/{id}/penalites?date= : pour chaque échéance impayée à la date, pénalité = dû × 0,1 % × jours de retard ;
    // elle s'ajoute au dû (jamais diminuée) et sera imputée en premier. 400 si la date est future, 409 si le prêt est soldé
    @Transactional
    public PenalitesDTO calculerPenalites(Long pretId, LocalDate date) {
        PretEntity pret = trouverOu404(pretRepository, pretId, "Prêt");
        if (date.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date " + date + " est dans le futur");
        }
        if (pret.getStatut() == StatutPret.SOLDE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le prêt " + pretId + " est soldé : aucune pénalité");
        }

        PenalitesDTO dto = new PenalitesDTO();
        dto.setPretId(pretId);
        dto.setDate(date);
        for (EcheanceEntity echeance : pret.getEcheances()) {
            BigDecimal montantDu = echeance.capitalDu().add(echeance.interetsDus());
            if (echeance.getDateEcheance().isBefore(date) && montantDu.signum() > 0) {
                long joursRetard = ChronoUnit.DAYS.between(echeance.getDateEcheance(), date);
                BigDecimal penalite = montantDu.multiply(TAUX_PENALITE_JOUR)
                        .multiply(BigDecimal.valueOf(joursRetard))
                        .setScale(2, RoundingMode.HALF_UP);
                echeance.setPenalites(echeance.getPenalites().max(penalite));   // recalculer ne double pas la pénalité

                PenalitesDTO.Ligne ligne = new PenalitesDTO.Ligne();
                ligne.setEcheanceId(echeance.getId());
                ligne.setNumero(echeance.numero());
                ligne.setDateEcheance(echeance.getDateEcheance());
                ligne.setMontantDu(montantDu);
                ligne.setJoursRetard(joursRetard);
                ligne.setPenalites(echeance.getPenalites());
                dto.getEcheances().add(ligne);
            }
            dto.setTotalPenalites(dto.getTotalPenalites().add(echeance.penalitesDues()));
        }
        return dto;
    }

    // POST /echeances : saisit une nouvelle échéance (INSERT), rattachée à son prêt
    public EcheanceResponseDTO creer(EcheanceRequestDTO dto) {
        EcheanceEntity echeance = new EcheanceEntity();
        dtoVersEntity(dto, echeance);
        return entityVersResponseDTO(echeanceRepository.save(echeance));
    }

    // PUT /echeances/{id} : charge l'échéance existante (404 sinon), corrige ses montants, puis UPDATE
    public EcheanceResponseDTO modifier(Long id, EcheanceRequestDTO dto) {
        EcheanceEntity echeance = trouverOu404(echeanceRepository, id, "Échéance");
        dtoVersEntity(dto, echeance);
        return entityVersResponseDTO(echeanceRepository.save(echeance));
    }

    // DELETE /echeances/{id} : vérifie que l'échéance existe (404 sinon), puis la supprime
    public void supprimer(Long id) {
        echeanceRepository.delete(trouverOu404(echeanceRepository, id, "Échéance"));
    }

    // DTO -> Entity (pretId est transformé en objet, 404 s'il n'existe pas ; le total est calculé)
    private void dtoVersEntity(EcheanceRequestDTO dto, EcheanceEntity echeance) {
        PretEntity pret = trouverOu404(pretRepository, dto.getPretId(), "Prêt");

        echeance.setDateEcheance(dto.getDateEcheance());
        echeance.setCapital(dto.getMontantCapital());
        echeance.setInterets(dto.getMontantInterets());
        echeance.setMontantTotal(dto.getMontantCapital().add(dto.getMontantInterets()));
        echeance.setPret(pret);
    }

    // Entity -> DTO
    private EcheanceResponseDTO entityVersResponseDTO(EcheanceEntity echeance) {
        EcheanceResponseDTO dto = new EcheanceResponseDTO();
        dto.setId(echeance.getId());
        dto.setNumero(echeance.numero());
        dto.setDateEcheance(echeance.getDateEcheance());
        dto.setMontantCapital(echeance.getCapital());
        dto.setMontantInterets(echeance.getInterets());
        dto.setMontantTotal(echeance.getMontantTotal());
        dto.setPenalites(echeance.getPenalites());   // F13
        dto.setMontantPaye(echeance.montantPaye());
        dto.setResteAPayer(echeance.resteAPayer());
        dto.setPretId(echeance.getPret().getId());
        return dto;
    }
}
