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

    // F8 : les courses d'un conducteur entre deux dates, triées par date puis heure de départ
    List<CourseEntity> findByConducteurIdAndDateCourseBetweenOrderByDateCourseAscHeureDepartAsc(Long conducteurId, LocalDate du, LocalDate au);
}