package com.projet3.atelierdeproduction.DTO;

import com.projet3.atelierdeproduction.Entity.StatutOrdre;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// F18 - ce qu'il faut sortir du magasin pour un ordre : par composant acheté, nécessaire, disponible et écart
@Getter
@Setter
@NoArgsConstructor
public class BesoinsOrdreDTO {

    private Long ordreId;
    private String articleReference;
    private Integer quantite;
    private StatutOrdre statut;
    private boolean besoinsReels;              // true pour un ordre terminé : quantités réellement consommées
    private List<Ligne> besoins = new ArrayList<>();

    // F18 - une ligne de besoin ; écart = disponible - nécessaire (négatif = manque)
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Ligne {
        private String reference;
        private String libelle;
        private BigDecimal quantiteNecessaire;
        private BigDecimal quantiteDisponible;
        private BigDecimal ecart;
    }
}
