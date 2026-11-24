package com.projet1.placedemarche.Service;

import com.projet1.placedemarche.DTO.AlerteStockDTO;
import com.projet1.placedemarche.Entity.ProduitEntity;
import com.projet1.placedemarche.Entity.StatutCommande;
import com.projet1.placedemarche.Repository.CommandeRepository;
import com.projet1.placedemarche.Repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlerteStockService {

    private static final Logger log = LoggerFactory.getLogger(AlerteStockService.class);

    private final ProduitRepository produitRepository;
    private final CommandeRepository commandeRepository;

    // F16 - tâche planifiée chaque nuit à 2h00 (cron : seconde minute heure jour mois jour-de-semaine).
    // Signale les produits dont le stock est <= 5 (ou nul), une alerte par vendeur ; un produit déjà signalé
    // ne l'est plus jusqu'au réapprovisionnement (alerteStockEnvoyee, remis à false par ProduitEntity.modifierStock)
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public List<AlerteStockDTO> signalerStocksFaibles() {
        List<ProduitEntity> produits =
                produitRepository.findByStockLessThanEqualAndAlerteStockEnvoyeeFalse(ProduitEntity.SEUIL_STOCK_FAIBLE);

        // F16 - regroupement par vendeur : une alerte par vendeur
        Map<Long, AlerteStockDTO> alertesParVendeur = new LinkedHashMap<>();
        for (ProduitEntity produit : produits) {
            AlerteStockDTO alerte = alertesParVendeur.computeIfAbsent(produit.getVendeur().getId(), idVendeur -> {
                AlerteStockDTO nouvelle = new AlerteStockDTO();
                nouvelle.setVendeurId(idVendeur);
                nouvelle.setVendeurRaisonSociale(produit.getVendeur().getRaisonSociale());
                return nouvelle;
            });

            AlerteStockDTO.Ligne ligne = new AlerteStockDTO.Ligne();
            ligne.setProduitId(produit.getId());
            ligne.setProduitNom(produit.getNom());
            ligne.setStockRestant(produit.getStock());
            ligne.setCommandesEnAttente(commandeRepository.countByProduitIdAndStatut(produit.getId(), StatutCommande.EN_ATTENTE));
            alerte.getProduits().add(ligne);

            produit.setAlerteStockEnvoyee(true);   // F16 - traçabilité : ne sera plus signalé avant réapprovisionnement
        }

        // F16 - "une alerte est produite" : écrite dans les logs de l'application
        alertesParVendeur.values().forEach(a ->
                log.warn("ALERTE STOCK FAIBLE - vendeur {} ({}) : {} produit(s)",
                        a.getVendeurRaisonSociale(), a.getVendeurId(), a.getProduits().size()));

        return new ArrayList<>(alertesParVendeur.values());
    }
}
