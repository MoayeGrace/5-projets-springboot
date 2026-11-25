package com.projet3.atelierdeproduction.Repository;

import com.projet3.atelierdeproduction.Entity.ArticleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleEntity, Long> {

    // Un composant de nomenclature est désigné par sa référence
    Optional<ArticleEntity> findByReference(String reference);
}
