package com.projet1.placedemarche.Repository;

import com.projet1.placedemarche.Entity.ProduitEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitRepository extends JpaRepository<ProduitEntity, Long> {

    // F6 : les produits d'un vendeur, triés par nom (SQL généré à partir du nom de la méthode)
    List<ProduitEntity> findByVendeurIdOrderByNomAsc(Long vendeurId);

    // F7 : les produits d'une catégorie, triés par prix croissant
    List<ProduitEntity> findByCategorieIdOrderByPrixAsc(Long categorieId);

    // F9 + F10 : recherche avec critères facultatifs (un critère null est ignoré), renvoyée page par page
    @Query("""
            SELECT p FROM ProduitEntity p
            WHERE (:nom IS NULL OR LOWER(p.nom) LIKE LOWER(CONCAT('%', :nom, '%')))
              AND (:prixMin IS NULL OR p.prix >= :prixMin)
              AND (:prixMax IS NULL OR p.prix <= :prixMax)
            """)
    Page<ProduitEntity> rechercher(@Param("nom") String nom,
                                   @Param("prixMin") BigDecimal prixMin,
                                   @Param("prixMax") BigDecimal prixMax,
                                   Pageable pageable);

    // F12 - lit le produit en posant un verrou sur sa ligne (SELECT ... FOR UPDATE) :
    // une 2e commande simultanée sur le même produit attend la fin de la 1re transaction
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProduitEntity p WHERE p.id = :id")
    Optional<ProduitEntity> findByIdAvecVerrou(@Param("id") Long id);
}
