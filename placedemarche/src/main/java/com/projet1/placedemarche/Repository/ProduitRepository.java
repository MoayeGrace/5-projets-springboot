package com.projet1.placedemarche.Repository;

import com.projet1.placedemarche.Entity.ProduitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProduitRepository extends JpaRepository<ProduitEntity, Long> {
}