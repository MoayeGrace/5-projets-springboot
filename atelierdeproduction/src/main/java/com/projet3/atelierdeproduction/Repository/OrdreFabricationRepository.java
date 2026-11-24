package com.projet3.atelierdeproduction.Repository;

import com.projet3.atelierdeproduction.Entity.OrdreFabricationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdreFabricationRepository extends JpaRepository<OrdreFabricationEntity, Long> {
}