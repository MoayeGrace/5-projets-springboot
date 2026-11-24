package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.DTO.AlerteRetardDTO;
import com.projet3.atelierdeproduction.Entity.OrdreFabricationEntity;
import com.projet3.atelierdeproduction.Entity.StatutOrdre;
import com.projet3.atelierdeproduction.Repository.OrdreFabricationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlerteRetardService {

    private static final Logger log = LoggerFactory.getLogger(AlerteRetardService.class);

    private final OrdreFabricationRepository ordreFabricationRepository;

    // F16 - tâche planifiée chaque nuit à 2h00 : signale les ordres dont la date prévue est passée et qui ne sont
    // ni TERMINE ni ANNULE ; une alerte par atelier ; le retard cumulé est conservé sur l'ordre
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public List<AlerteRetardDTO> signalerRetards() {
        LocalDateTime maintenant = LocalDateTime.now();
        List<OrdreFabricationEntity> ordres = ordreFabricationRepository.findByDatePrevueBeforeAndStatutInOrderByDatePrevueAsc(
                maintenant, List.of(StatutOrdre.PLANIFIE, StatutOrdre.LANCE));

        Map<String, AlerteRetardDTO> parAtelier = new LinkedHashMap<>();
        for (OrdreFabricationEntity ordre : ordres) {
            long retard = ChronoUnit.DAYS.between(ordre.getDatePrevue().toLocalDate(), maintenant.toLocalDate());
            ordre.setRetardJours((int) retard);   // F16 - traçabilité : retard cumulé conservé sur l'ordre

            AlerteRetardDTO.Ligne ligne = new AlerteRetardDTO.Ligne();
            ligne.setOrdreId(ordre.getId());
            ligne.setArticleReference(ordre.getArticle().getReference());
            ligne.setRetardJours(retard);
            ligne.setStatut(ordre.getStatut());
            parAtelier.computeIfAbsent(ordre.getMachine().getAtelier(), atelier -> {
                AlerteRetardDTO alerte = new AlerteRetardDTO();
                alerte.setAtelier(atelier);
                return alerte;
            }).getOrdres().add(ligne);
        }

        // F16 - "une alerte est produite" : écrite dans les logs de l'application
        parAtelier.values().forEach(a ->
                log.warn("ALERTE RETARD - atelier {} : {} ordre(s) en retard", a.getAtelier(), a.getOrdres().size()));
        return new ArrayList<>(parAtelier.values());
    }
}
