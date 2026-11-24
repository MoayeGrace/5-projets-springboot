package com.projet3.atelierdeproduction.Repository;

import com.projet3.atelierdeproduction.Entity.LigneNomenclatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigneNomenclatureRepository extends JpaRepository<LigneNomenclatureEntity, Long> {

    // F6 : composants directs d'un article, triés par référence du composant
    List<LigneNomenclatureEntity> findByComposeIdOrderByComposantReferenceAsc(Long composeId);
}