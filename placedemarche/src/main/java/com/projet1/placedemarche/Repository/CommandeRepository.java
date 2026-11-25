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

    // F8 : les commandes d'un client, de la plus récemment modifiée à la plus ancienne (à égalité, la plus récente d'abord)
    List<CommandeEntity> findByClientIdOrderByDateModificationDescIdDesc(Long clientId);

    // F16 - nombre de commandes d'un produit dans un statut donné (ici EN_ATTENTE)
    long countByProduitIdAndStatut(Long produitId, StatutCommande statut);

    // F17 - commandes LIVRÉES d'un vendeur livrées dans la période (LIVREE est final : la date de modification est celle de la livraison)
    @Query("""
            SELECT c FROM CommandeEntity c
            WHERE c.produit.vendeur.id = :vendeurId
              AND c.statut = :statut
              AND c.dateModification >= :debut AND c.dateModification < :fin
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
              AND c.dateModification >= :debut AND c.dateModification < :fin
            GROUP BY p.id, p.nom, v.raisonSociale
            ORDER BY SUM(c.quantite) DESC
            """)
    List<TopProduitDTO> topProduits(@Param("exclu") StatutCommande exclu,
                                    @Param("debut") LocalDateTime debut,
                                    @Param("fin") LocalDateTime fin,
                                    Pageable limite);

    // F19 - commandes dans un statut donné (ici PAYEE), triées par vendeur pour faciliter le regroupement
    @Query("SELECT c FROM CommandeEntity c WHERE c.statut = :statut ORDER BY c.produit.vendeur.raisonSociale, c.id")
    List<CommandeEntity> parStatutTrieesParVendeur(@Param("statut") StatutCommande statut);
}
