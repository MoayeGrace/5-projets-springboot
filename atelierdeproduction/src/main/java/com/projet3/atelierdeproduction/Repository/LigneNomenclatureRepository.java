package com.projet3.atelierdeproduction.Repository;

import com.projet3.atelierdeproduction.Entity.LigneNomenclatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LigneNomenclatureRepository extends JpaRepository<LigneNomenclatureEntity, Long> {

    // F6 / F15 : les lignes d'une version de la nomenclature d'un article, triées par référence du composant
    List<LigneNomenclatureEntity> findByArticleComposeIdAndVersionOrderByComposantReferenceAsc(Long articleId, Integer version);

    // F15 : toutes les lignes d'un article, version par version
    List<LigneNomenclatureEntity> findByArticleComposeIdOrderByVersionAscComposantReferenceAsc(Long articleId);

    // F7 : lignes (toutes versions) où une référence apparaît comme composant, triées par référence du composé
    List<LigneNomenclatureEntity> findByComposantReferenceOrderByArticleComposeReferenceAsc(String composantReference);

    // Une référence est-elle utilisée comme composant ?
    boolean existsByComposantReference(String composantReference);

    // F15 : version applicable à une date = la plus récente des versions appliquées au plus tard ce jour-là (null si aucune)
    @Query("""
            SELECT MAX(l.version) FROM LigneNomenclatureEntity l
            WHERE l.articleCompose.id = :articleId AND l.dateApplication IS NOT NULL AND l.dateApplication <= :date
            """)
    Integer versionApplicable(@Param("articleId") Long articleId, @Param("date") LocalDate date);

    // F15 : version en préparation (date d'application vide), null si aucune
    @Query("SELECT MAX(l.version) FROM LigneNomenclatureEntity l WHERE l.articleCompose.id = :articleId AND l.dateApplication IS NULL")
    Integer versionEnPreparation(@Param("articleId") Long articleId);

    // F15 : numéro de la dernière version créée, null si l'article n'a pas de nomenclature
    @Query("SELECT MAX(l.version) FROM LigneNomenclatureEntity l WHERE l.articleCompose.id = :articleId")
    Integer derniereVersion(@Param("articleId") Long articleId);
}
