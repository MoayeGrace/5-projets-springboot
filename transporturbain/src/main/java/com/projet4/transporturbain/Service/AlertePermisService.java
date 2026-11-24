package com.projet4.transporturbain.Service;

import com.projet4.transporturbain.DTO.AlertePermisDTO;
import com.projet4.transporturbain.Entity.ConducteurEntity;
import com.projet4.transporturbain.Entity.CourseEntity;
import com.projet4.transporturbain.Entity.StatutCourse;
import com.projet4.transporturbain.Repository.ConducteurRepository;
import com.projet4.transporturbain.Repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertePermisService {

    private static final Logger log = LoggerFactory.getLogger(AlertePermisService.class);

    // F16 - un permis est signalé s'il expire dans ce nombre de jours
    private static final int DELAI_ALERTE_JOURS = 60;

    private final ConducteurRepository conducteurRepository;
    private final CourseRepository courseRepository;

    // F16 - tâche planifiée chaque nuit à 2h00 : signale les permis qui expirent dans les 60 jours, avec les courses
    // déjà programmées au-delà ; un conducteur déjà signalé ne l'est plus tant que son permis n'est pas renouvelé
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public List<AlertePermisDTO> signalerPermis() {
        LocalDate aujourdhui = LocalDate.now();
        List<AlertePermisDTO> alertes = new ArrayList<>();
        for (ConducteurEntity conducteur : conducteurRepository
                .findByDateValiditePermisBetweenAndAlertePermisEnvoyeeFalseOrderByDateValiditePermisAsc(
                        aujourdhui, aujourdhui.plusDays(DELAI_ALERTE_JOURS))) {
            AlertePermisDTO alerte = new AlertePermisDTO();
            alerte.setConducteurId(conducteur.getId());
            alerte.setConducteurNom(conducteur.getNom());
            alerte.setDateExpiration(conducteur.getDateValiditePermis());
            for (CourseEntity course : courseRepository.findByConducteurIdAndDateCourseAfterAndStatutOrderByDateCourseAscHeureDepartAsc(
                    conducteur.getId(), conducteur.getDateValiditePermis(), StatutCourse.PREVUE)) {
                AlertePermisDTO.CourseProgrammee ligne = new AlertePermisDTO.CourseProgrammee();
                ligne.setCourseId(course.getId());
                ligne.setLigneNumero(course.getLigne().getNumero());
                ligne.setDateCourse(course.getDateCourse());
                ligne.setHeureDepart(course.getHeureDepart());
                alerte.getCoursesApresExpiration().add(ligne);
            }
            conducteur.setAlertePermisEnvoyee(true);   // F16 - traçabilité : pas de second signalement
            alertes.add(alerte);
            // F16 - "une alerte est produite" : écrite dans les logs de l'application
            log.warn("ALERTE PERMIS - {} : permis expirant le {}, {} course(s) programmée(s) au-delà",
                    conducteur.getNom(), conducteur.getDateValiditePermis(), alerte.getCoursesApresExpiration().size());
        }
        return alertes;
    }
}
