package com.projet4.transporturbain.Repository;

import com.projet4.transporturbain.Entity.CourseEntity;
import com.projet4.transporturbain.Entity.StatutCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    // F7 : les courses d'une ligne sur une journée, triées par heure de départ
    List<CourseEntity> findByLigneIdAndDateCourseOrderByHeureDepartAsc(Long ligneId, LocalDate dateCourse);

    // F12 : les courses d'un bus qui n'ont pas un statut donné (ex : hors annulées)
    List<CourseEntity> findByBusIdAndStatutNot(Long busId, StatutCourse statut);

    // F13 : les courses d'un conducteur qui n'ont pas un statut donné (ex : hors annulées)
    List<CourseEntity> findByConducteurIdAndStatutNot(Long conducteurId, StatutCourse statut);

    // F16 : courses d'un conducteur programmées après une date, avec un statut donné (ex : PREVUE)
    List<CourseEntity> findByConducteurIdAndDateCourseAfterAndStatutOrderByDateCourseAscHeureDepartAsc(
            Long conducteurId, LocalDate date, StatutCourse statut);

    // F8 : les courses d'un conducteur entre deux dates, triées par date puis heure de départ
    List<CourseEntity> findByConducteurIdAndDateCourseBetweenOrderByDateCourseAscHeureDepartAsc(Long conducteurId, LocalDate du, LocalDate au);
}