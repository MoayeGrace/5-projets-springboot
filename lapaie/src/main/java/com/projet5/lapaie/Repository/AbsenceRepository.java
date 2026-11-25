package com.projet5.lapaie.Repository;

import com.projet5.lapaie.Entity.AbsenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbsenceRepository extends JpaRepository<AbsenceEntity, Long> {

    // F8 : absences d'un employé qui touchent la période [du, au] (même partiellement), de la plus récente à la plus ancienne
    List<AbsenceEntity> findByEmployeIdAndDateDebutLessThanEqualAndDateFinGreaterThanEqualOrderByDateDebutDesc(
            Long employeId, LocalDate au, LocalDate du);
}