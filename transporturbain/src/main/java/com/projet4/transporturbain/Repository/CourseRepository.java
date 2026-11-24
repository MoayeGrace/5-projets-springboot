package com.projet4.transporturbain.Repository;

import com.projet4.transporturbain.Entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    // F7 : les courses d'une ligne sur une journée, triées par heure de départ
    List<CourseEntity> findByLigneIdAndDateCourseOrderByHeureDepartAsc(Long ligneId, LocalDate dateCourse);
}