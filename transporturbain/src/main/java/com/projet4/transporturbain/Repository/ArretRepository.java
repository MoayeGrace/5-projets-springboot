package com.projet4.transporturbain.Repository;

import com.projet4.transporturbain.Entity.ArretEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArretRepository extends JpaRepository<ArretEntity, Long> {

    // F9 : tous les arrêts, triés par nom
    List<ArretEntity> findAllByOrderByNomAsc();

    // F9 : les arrêts d'une commune (sans tenir compte des majuscules), triés par nom
    List<ArretEntity> findByCommuneIgnoreCaseOrderByNomAsc(String commune);
}