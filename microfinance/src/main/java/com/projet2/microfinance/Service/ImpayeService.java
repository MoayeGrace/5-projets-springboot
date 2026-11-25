package com.projet2.microfinance.Service;

import com.projet2.microfinance.DTO.AlerteImpayeDTO;
import com.projet2.microfinance.DTO.ImpayeClientDTO;
import com.projet2.microfinance.Entity.EcheanceEntity;
import com.projet2.microfinance.Entity.PretEntity;
import com.projet2.microfinance.Repository.EcheanceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImpayeService {

    private static final Logger log = LoggerFactory.getLogger(ImpayeService.class);

    private final EcheanceRepository echeanceRepository;

    // F16 - tâche planifiée chaque nuit à 3h00 (cron : seconde minute heure jour mois jour-de-semaine).
    // Une alerte par agent listant ses clients en impayé ; le retard maximal atteint est conservé sur chaque prêt
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public List<AlerteImpayeDTO> detecterImpayes() {
        LocalDate aujourdhui = LocalDate.now();
        Map<String, Map<Long, ImpayeClientDTO>> parAgent = new LinkedHashMap<>();

        for (EcheanceEntity echeance : echeanceRepository.findImpayees(aujourdhui)) {
            PretEntity pret = echeance.getPret();
            ImpayeClientDTO client = parAgent
                    .computeIfAbsent(pret.getClient().getAgentCredit(), agent -> new LinkedHashMap<>())
                    .computeIfAbsent(pret.getClient().getId(), id -> nouveauClient(pret));
            long jours = ajouterEcheance(client, echeance, aujourdhui);

            // F16 - traçabilité : on garde sur le prêt le plus grand retard jamais atteint
            pret.setJoursRetardMax(Math.max(pret.getJoursRetardMax(), (int) jours));
        }

        List<AlerteImpayeDTO> alertes = new ArrayList<>();
        parAgent.forEach((agent, clients) -> {
            AlerteImpayeDTO alerte = new AlerteImpayeDTO();
            alerte.setAgent(agent);
            alerte.getClients().addAll(clients.values());
            alertes.add(alerte);
            // F16 - "une alerte est produite" : écrite dans les logs de l'application
            log.warn("ALERTE IMPAYÉS - agent {} : {} client(s) en retard", agent, clients.size());
        });
        return alertes;
    }

    // F18 - GET /echeances/impayees?date= : échéances antérieures à la date avec un reste à payer, groupées par client
    // (montant dû et plus grand retard) ; les prêts soldés sont exclus par la requête
    public List<ImpayeClientDTO> impayesParClient(LocalDate date) {
        Map<Long, ImpayeClientDTO> parClient = new LinkedHashMap<>();
        for (EcheanceEntity echeance : echeanceRepository.findImpayees(date)) {
            PretEntity pret = echeance.getPret();
            ImpayeClientDTO client = parClient.computeIfAbsent(pret.getClient().getId(), id -> nouveauClient(pret));
            ajouterEcheance(client, echeance, date);
        }
        return new ArrayList<>(parClient.values());
    }

    // F16 - nouvelle ligne client, vide
    private ImpayeClientDTO nouveauClient(PretEntity pret) {
        ImpayeClientDTO client = new ImpayeClientDTO();
        client.setClientId(pret.getClient().getId());
        client.setClientNom(pret.getClient().getNom());
        return client;
    }

    // F16 - ajoute le reste à payer de l'échéance au dû du client, met à jour son plus grand retard et renvoie le retard
    private long ajouterEcheance(ImpayeClientDTO client, EcheanceEntity echeance, LocalDate date) {
        long jours = ChronoUnit.DAYS.between(echeance.getDateEcheance(), date);
        client.setMontantDu(client.getMontantDu().add(echeance.resteAPayer()));
        client.setJoursRetardMax(Math.max(client.getJoursRetardMax(), jours));
        return jours;
    }
}
