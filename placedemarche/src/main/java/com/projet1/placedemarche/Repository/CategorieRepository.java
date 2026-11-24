package com.projet1.placedemarche.Repository;

import com.projet1.placedemarche.Entity.CategorieEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategorieRepository extends JpaRepository<CategorieEntity, Long> {
}