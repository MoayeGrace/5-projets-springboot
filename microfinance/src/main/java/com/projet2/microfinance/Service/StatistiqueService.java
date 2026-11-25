package com.projet2.microfinance.Service;

import com.projet2.microfinance.DTO.EncoursDTO;
import com.projet2.microfinance.DTO.SuiviPretDTO;
import com.projet2.microfinance.Entity.ClientEntity;
import com.projet2.microfinance.Entity.EcheanceEntity;
import com.projet2.microfinance.Entity.PaiementEntity;
import com.projet2.microfinance.Entity.PretEntity;
import com.projet2.microfinance.Entity.StatutPret;
import com.projet2.microfinance.Repository.ClientRepository;
import com.projet2.microfinance.Repository.PretRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.projet2.microfinance.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class StatistiqueService {

    private final ClientRepository clientRepository;
    private final PretRepository pretRepository;

    // F17 - GET /clients/{id}/encours : capital restant dû, intérêts à venir, pénalités dues et déjà remboursé,
    // agrégés sur tous les prêts EN_COURS du client (0 partout s'il n'en a aucun ; 404 si le client n'existe pas)
    public EncoursDTO encoursClient(Long clientId) {
        ClientEntity client = trouverOu404(clientRepository, clientId, "Client");
        EncoursDTO dto = new EncoursDTO();
        dto.setClientId(client.getId());
        dto.setClientNom(client.getNom());

        for (PretEntity pret : pretRepository.findByClientIdAndStatut(clientId, StatutPret.EN_COURS)) {
            EncoursDTO.Montants ligne = new EncoursDTO.Montants();
            ligne.setPretId(pret.getId());
            ligne.setCapitalRestantDu(somme(pret, EcheanceEntity::capitalDu));
            ligne.setInteretsAVenir(somme(pret, EcheanceEntity::interetsDus));
            ligne.setPenalitesDues(somme(pret, EcheanceEntity::penalitesDues));
            ligne.setMontantRembourse(pret.getPaiements().stream()
                    .map(PaiementEntity::getMontant)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.getPrets().add(ligne);
            dto.getTotal().ajouter(ligne);
        }
        return dto;
    }

    // F19 - GET /prets/{id}/suivi : pour chaque échéance le prévu, le payé, le reste et le retard ; plus une synthèse
    // (total remboursé, capital restant, taux d'avancement). 409 si l'échéancier n'est pas encore généré
    public SuiviPretDTO suiviPret(Long pretId) {
        PretEntity pret = trouverOu404(pretRepository, pretId, "Prêt");
        if (pret.getEcheances().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le prêt " + pretId + " n'a pas d'échéancier : générez-le d'abord (POST /prets/" + pretId + "/echeancier)");
        }

        SuiviPretDTO dto = new SuiviPretDTO();
        dto.setPretId(pret.getId());
        dto.setClientNom(pret.getClient().getNom());
        dto.setStatut(pret.getStatut());
        dto.setMontant(pret.getMontant());
        dto.setTotalRembourse(pret.getPaiements().stream()
                .map(PaiementEntity::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        dto.setCapitalRestant(pret.capitalRestantDu());
        dto.setTauxAvancement(pret.getMontant().subtract(pret.capitalRestantDu())
                .multiply(BigDecimal.valueOf(100))
                .divide(pret.getMontant(), 2, RoundingMode.HALF_UP));

        Map<EcheanceEntity, LocalDate> datesReglement = datesReglement(pret);
        LocalDate aujourdhui = LocalDate.now();
        for (EcheanceEntity echeance : pret.getEcheances()) {
            SuiviPretDTO.Ligne ligne = new SuiviPretDTO.Ligne();
            ligne.setNumero(echeance.numero());
            ligne.setDateEcheance(echeance.getDateEcheance());
            ligne.setPrevu(echeance.getMontantTotal().add(echeance.getPenalites()));
            ligne.setPaye(echeance.montantPaye());
            ligne.setResteAPayer(echeance.resteAPayer());
            ligne.setJoursRetard(joursRetard(echeance, datesReglement.getOrDefault(echeance, aujourdhui)));
            dto.getEcheances().add(ligne);
        }
        return dto;
    }

    // F19 - date de règlement de chaque échéance soldée : les paiements sont imputés de la plus ancienne échéance à la plus
    // récente, donc une échéance est réglée par le premier paiement dont le cumul couvre tout le dû jusqu'à elle
    private Map<EcheanceEntity, LocalDate> datesReglement(PretEntity pret) {
        List<PaiementEntity> paiements = pret.getPaiements().stream()
                .sorted(Comparator.comparing(PaiementEntity::getDatePaiement).thenComparing(PaiementEntity::getId))
                .toList();
        Map<EcheanceEntity, LocalDate> dates = new HashMap<>();
        BigDecimal duCumule = BigDecimal.ZERO;
        BigDecimal payeCumule = BigDecimal.ZERO;
        int indexPaiement = 0;
        for (EcheanceEntity echeance : pret.getEcheances()) {
            if (echeance.resteAPayer().signum() > 0) {
                break;   // les suivantes ne sont pas soldées non plus
            }
            duCumule = duCumule.add(echeance.getMontantTotal()).add(echeance.getPenalites());
            while (payeCumule.compareTo(duCumule) < 0 && indexPaiement < paiements.size()) {
                payeCumule = payeCumule.add(paiements.get(indexPaiement++).getMontant());
            }
            if (indexPaiement > 0) {
                dates.put(echeance, paiements.get(indexPaiement - 1).getDatePaiement());
            }
        }
        return dates;
    }

    // F19 - retard constaté : de la date d'échéance jusqu'à son règlement (ou jusqu'à aujourd'hui si elle reste due)
    private long joursRetard(EcheanceEntity echeance, LocalDate fin) {
        return Math.max(0, ChronoUnit.DAYS.between(echeance.getDateEcheance(), fin));
    }

    // F17 - additionne une valeur calculée sur chaque échéance du prêt
    private BigDecimal somme(PretEntity pret, Function<EcheanceEntity, BigDecimal> valeur) {
        return pret.getEcheances().stream()
                .map(valeur)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
