package com.projet1.placedemarche.Repository;

import com.projet1.placedemarche.DTO.TopProduitDTO;
import com.projet1.placedemarche.Entity.CommandeEntity;
import com.projet1.placedemarche.Entity.StatutCommande;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommandeRepository extends JpaRepository<CommandeEntity, Long> {

    // F8 : les commandes d'un client, de la plus récente à la plus ancienne
    List<CommandeEntity> findByClientIdOrderByDateCommandeDesc(Long clientId);

    // F16 - nombre de commandes d'un produit dans un statut donné (ici EN_ATTENTE)
    long countByProduitIdAndStatut(Long produitId, StatutCommande statut);

    // F17 - commandes LIVRÉES d'un vendeur dont la livraison tombe dans la période
    @Query("""
            SELECT c FROM CommandeEntity c
            WHERE c.produit.vendeur.id = :vendeurId
              AND c.statut = :statut
              AND c.dateLivraison >= :debut AND c.dateLivraison < :fin
            """)
    List<CommandeEntity> livreesParVendeur(@Param("vendeurId") Long vendeurId,
                                           @Param("statut") StatutCommande statut,
                                           @Param("debut") LocalDateTime debut,
                                           @Param("fin") LocalDateTime fin);

    // F18 - classement des produits par quantité commandée sur la période (hors statut exclu = ANNULEE).
    // "SELECT new ..." construit directement les TopProduitDTO ; le Pageable sert à limiter le nombre de lignes
    @Query("""
            SELECT new com.projet1.placedemarche.DTO.TopProduitDTO(
                       p.id, p.nom, v.raisonSociale, SUM(c.quantite), SUM(c.prixUnitaire * c.quantite))
            FROM CommandeEntity c JOIN c.produit p JOIN p.vendeur v
            WHERE c.statut <> :exclu
              AND c.dateCommande >= :debut AND c.dateCommande < :fin
            GROUP BY p.id, p.nom, v.raisonSociale
            ORDER BY SUM(c.quantite) DESC
            """)
    List<TopProduitDTO> topProduits(@Param("exclu") StatutCommande exclu,
                                    @Param("debut") LocalDateTime debut,
                                    @Param("fin") LocalDateTime fin,
                                    Pageable limite);
}
