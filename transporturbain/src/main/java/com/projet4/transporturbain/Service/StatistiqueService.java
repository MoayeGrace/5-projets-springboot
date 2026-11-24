package com.projet4.transporturbain.Service;

import com.projet4.transporturbain.DTO.PonctualiteDTO;
import com.projet4.transporturbain.Entity.CourseEntity;
import com.projet4.transporturbain.Entity.LigneEntity;
import com.projet4.transporturbain.Entity.StatutCourse;
import com.projet4.transporturbain.Repository.CourseRepository;
import com.projet4.transporturbain.Repository.LigneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.projet4.transporturbain.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class StatistiqueService {

    // F18 - une course est ponctuelle si elle arrive au plus 5 minutes après l'horaire prévu
    private static final long TOLERANCE_MINUTES = 5;

    private final LigneRepository ligneRepository;
    private final CourseRepository courseRepository;

    // F18 - GET /lignes/{id}/ponctualite?mois= : part des courses terminées arrivées dans les 5 minutes suivant l'horaire,
    // retard moyen et détail par tranche horaire de départ ; courses annulées ou sans heure réelle exclues
    public PonctualiteDTO ponctualite(Long ligneId, YearMonth mois) {
        LigneEntity ligne = trouverOu404(ligneRepository, ligneId, "Ligne");
        YearMonth periode = (mois != null) ? mois : YearMonth.now();
        List<CourseEntity> courses = courseRepository
                .findByLigneIdAndStatutAndDateCourseBetweenOrderByDateCourseAscHeureDepartAsc(
                        ligneId, StatutCourse.TERMINEE, periode.atDay(1), periode.atEndOfMonth())
                .stream()
                .filter(c -> c.getHeureArriveeReelle() != null)
                .toList();

        PonctualiteDTO dto = new PonctualiteDTO();
        dto.setLigneId(ligne.getId());
        dto.setLigneNumero(ligne.getNumero());
        dto.setMois(periode);

        long cumulRetards = 0;
        Map<Integer, int[]> parHeure = new TreeMap<>();   // heure de départ -> {courses, ponctuelles}
        for (CourseEntity course : courses) {
            long retard = Math.max(0, retardMinutes(course));
            boolean ponctuelle = retard <= TOLERANCE_MINUTES;
            cumulRetards += retard;
            dto.setNombreCourses(dto.getNombreCourses() + 1);
            int[] compteurs = parHeure.computeIfAbsent(course.getHeureDepart().getHour(), h -> new int[2]);
            compteurs[0]++;
            if (ponctuelle) {
                dto.setNombrePonctuelles(dto.getNombrePonctuelles() + 1);
                compteurs[1]++;
            }
        }
        dto.setTauxPonctualite(pourcentage(dto.getNombrePonctuelles(), dto.getNombreCourses()));
        if (dto.getNombreCourses() > 0) {
            dto.setRetardMoyenMinutes(BigDecimal.valueOf(cumulRetards)
                    .divide(BigDecimal.valueOf(dto.getNombreCourses()), 2, RoundingMode.HALF_UP));
        }
        parHeure.forEach((heure, compteurs) -> {
            PonctualiteDTO.Tranche tranche = new PonctualiteDTO.Tranche();
            tranche.setTranche(String.format("%02dh-%02dh", heure, heure + 1));
            tranche.setNombreCourses(compteurs[0]);
            tranche.setNombrePonctuelles(compteurs[1]);
            tranche.setTauxPonctualite(pourcentage(compteurs[1], compteurs[0]));
            dto.getTranches().add(tranche);
        });
        return dto;
    }

    // F18 - écart en minutes entre l'arrivée réelle et l'arrivée prévue (négatif = en avance) ; gère le passage de minuit
    private long retardMinutes(CourseEntity course) {
        LocalDateTime reelle = course.getDateCourse().atTime(course.getHeureArriveeReelle());
        if (reelle.isBefore(course.depart())) {
            reelle = reelle.plusDays(1);
        }
        return Duration.between(course.arriveePrevue(), reelle).toMinutes();
    }

    // Pourcentage arrondi à 2 décimales ; 0 si le total est nul
    private BigDecimal pourcentage(long partie, long total) {
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(partie * 100).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}
