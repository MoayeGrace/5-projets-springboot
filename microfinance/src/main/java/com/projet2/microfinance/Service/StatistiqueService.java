package com.projet2.microfinance.Service;

import com.projet2.microfinance.DTO.EncoursDTO;
import com.projet2.microfinance.Entity.ClientEntity;
import com.projet2.microfinance.Entity.EcheanceEntity;
import com.projet2.microfinance.Entity.PaiementEntity;
import com.projet2.microfinance.Entity.PretEntity;
import com.projet2.microfinance.Entity.StatutPret;
import com.projet2.microfinance.Repository.ClientRepository;
import com.projet2.microfinance.Repository.PretRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    // F17 - additionne une valeur calculée sur chaque échéance du prêt
    private BigDecimal somme(PretEntity pret, Function<EcheanceEntity, BigDecimal> valeur) {
        return pret.getEcheances().stream()
                .map(valeur)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
