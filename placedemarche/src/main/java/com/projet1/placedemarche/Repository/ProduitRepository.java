package com.projet1.placedemarche.Repository;

import com.projet1.placedemarche.Entity.ProduitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<ProduitEntity, Long> {

    // F6 : les produits d'un vendeur, triés par nom (SQL généré à partir du nom de la méthode)
    List<ProduitEntity> findByVendeurIdOrderByNomAsc(Long vendeurId);
}