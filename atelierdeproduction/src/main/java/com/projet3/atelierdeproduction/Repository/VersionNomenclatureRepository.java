package com.projet3.atelierdeproduction.Repository;

import com.projet3.atelierdeproduction.Entity.StatutVersion;
import com.projet3.atelierdeproduction.Entity.VersionNomenclatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// F15 - versions datées des nomenclatures
@Repository
public interface VersionNomenclatureRepository extends JpaRepository<VersionNomenclatureEntity, Long> {

    // F15 : toutes les versions d'un article, de la plus ancienne à la plus récente
    List<VersionNomenclatureEntity> findByArticleIdOrderByNumeroAsc(Long articleId);

    // F15 : la version d'un article ayant un statut donné (ex : celle en préparation)
    Optional<VersionNomenclatureEntity> findFirstByArticleIdAndStatut(Long articleId, StatutVersion statut);

    // F15 : la dernière version appliquée au plus tard à une date donnée = celle en vigueur à cette date
    Optional<VersionNomenclatureEntity> findFirstByArticleIdAndStatutAndDateApplicationLessThanEqualOrderByDateApplicationDesc(
            Long articleId, StatutVersion statut, LocalDateTime date);
}
