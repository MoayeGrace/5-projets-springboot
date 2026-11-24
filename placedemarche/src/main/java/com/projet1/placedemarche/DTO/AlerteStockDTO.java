package com.projet1.placedemarche.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// F16 - une alerte par vendeur, listant ses produits en stock faible
@Getter
@Setter
@NoArgsConstructor
public class AlerteStockDTO {

    private Long vendeurId;
    private String vendeurRaisonSociale;
    private List<Ligne> produits = new ArrayList<>();

    // F16 - une ligne par produit : le produit, le stock restant et les commandes en attente
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Ligne {
        private Long produitId;
        private String produitNom;
        private Integer stockRestant;
        private long commandesEnAttente;
    }
}
