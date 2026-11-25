package com.projet5.lapaie.Repository;

import com.projet5.lapaie.Entity.EmployeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeRepository extends JpaRepository<EmployeEntity, Long> {

    // F9 : recherche avec critères facultatifs (un critère null est ignoré) : matricule partiel, service exact,
    // sans tenir compte des majuscules
    @Query("""
            SELECT e FROM EmployeEntity e
            WHERE (:matricule IS NULL OR LOWER(e.matricule) LIKE LOWER(CONCAT('%', :matricule, '%')))
              AND (:service IS NULL OR LOWER(e.service) = LOWER(:service))
            ORDER BY e.matricule
            """)
    List<EmployeEntity> rechercher(@Param("matricule") String matricule, @Param("service") String service);
}