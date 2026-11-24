package com.projet1.placedemarche.Repository;

import com.projet1.placedemarche.Entity.CommandeEntity;
import com.projet1.placedemarche.Entity.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandeRepository extends JpaRepository<CommandeEntity, Long> {

    // F8 : les commandes d'un client, de la plus récente à la plus ancienne
    List<CommandeEntity> findByClientIdOrderByDateCommandeDesc(Long clientId);

    // F16 - nombre de commandes d'un produit dans un statut donné (ici EN_ATTENTE)
    long countByProduitIdAndStatut(Long produitId, StatutCommande statut);
}
