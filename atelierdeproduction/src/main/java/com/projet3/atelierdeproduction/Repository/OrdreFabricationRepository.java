package com.projet3.atelierdeproduction.Repository;

import com.projet3.atelierdeproduction.Entity.OrdreFabricationEntity;
import com.projet3.atelierdeproduction.Entity.StatutOrdre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdreFabricationRepository extends JpaRepository<OrdreFabricationEntity, Long> {

    // F8 : tous les ordres, triés par date prévue
    List<OrdreFabricationEntity> findAllByOrderByDatePrevueAsc();

    // F8 : les ordres d'un statut donné, triés par date prévue
    List<OrdreFabricationEntity> findByStatutOrderByDatePrevueAsc(StatutOrdre statut);
}