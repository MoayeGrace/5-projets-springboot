package com.projet3.atelierdeproduction.Repository;

import com.projet3.atelierdeproduction.Entity.MaintenanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// F19 - arrêts de maintenance des machines
@Repository
public interface MaintenanceRepository extends JpaRepository<MaintenanceEntity, Long> {

    // F19 : les arrêts d'une machine, triés par date de début
    List<MaintenanceEntity> findByMachineIdOrderByDebutAsc(Long machineId);
}
