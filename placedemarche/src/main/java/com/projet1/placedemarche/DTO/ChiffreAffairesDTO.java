package com.projet1.placedemarche.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// F17 - chiffre d'affaires d'un vendeur sur un mois : total, nombre de commandes, détail par produit
@Getter
@Setter
@NoArgsConstructor
public class ChiffreAffairesDTO {

    private Long vendeurId;
    private String mois;                          // ex : "2026-11"
    private BigDecimal total = BigDecimal.ZERO;   // 0 si aucune vente
    private int nombreCommandes;
    private List<Ligne> detail = new ArrayList<>();

    // F17 - détail par produit
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Ligne {
        private Long produitId;
        private String produitNom;
        private long quantite;
        private BigDecimal montant = BigDecimal.ZERO;
    }
}
