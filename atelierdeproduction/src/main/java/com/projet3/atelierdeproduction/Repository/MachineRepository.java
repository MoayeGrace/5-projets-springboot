package com.projet3.atelierdeproduction.Repository;

import com.projet3.atelierdeproduction.Entity.MachineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MachineRepository extends JpaRepository<MachineEntity, Long> {
}