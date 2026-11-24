package com.projet1.placedemarche.Repository;

import com.projet1.placedemarche.Entity.CommandeEntity;
import com.projet1.placedemarche.Entity.StatutCommande;
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
}
