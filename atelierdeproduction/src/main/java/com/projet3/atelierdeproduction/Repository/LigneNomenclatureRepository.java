package com.projet3.atelierdeproduction.Repository;

import com.projet3.atelierdeproduction.Entity.LigneNomenclatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LigneNomenclatureRepository extends JpaRepository<LigneNomenclatureEntity, Long> {
}