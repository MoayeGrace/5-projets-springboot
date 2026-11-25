package com.projet5.lapaie.Repository;

import com.projet5.lapaie.Entity.ContratEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratRepository extends JpaRepository<ContratEntity, Long> {

    // F6 : les contrats d'un employé, du plus récent au plus ancien
    List<ContratEntity> findByEmployeIdOrderByDateDebutDesc(Long employeId);

    // F11 : tous les contrats d'un employé
    List<ContratEntity> findByEmployeId(Long employeId);
}