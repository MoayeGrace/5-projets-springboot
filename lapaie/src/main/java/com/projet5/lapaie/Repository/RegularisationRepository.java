package com.projet5.lapaie.Repository;

import com.projet5.lapaie.Entity.RegularisationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// F15 - régularisations de bulletins édités
@Repository
public interface RegularisationRepository extends JpaRepository<RegularisationEntity, Long> {

    // F15 : régularisations d'un employé encore en attente d'un bulletin pour les porter
    List<RegularisationEntity> findByBulletinOrigineEmployeIdAndBulletinCibleIsNullOrderByIdAsc(Long employeId);
}
