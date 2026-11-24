package com.projet3.atelierdeproduction.DTO;

import com.projet3.atelierdeproduction.Entity.TypeArticle;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// F17 - nomenclature déployée : l'arbre complet, la profondeur atteinte et la liste à plat des composants achetables
@Getter
@Setter
@NoArgsConstructor
public class NomenclatureCompleteDTO {

    private Noeud arbre;
    private int profondeur;                                // nombre de niveaux sous l'article (0 pour un article acheté)
    private List<Besoin> achetables = new ArrayList<>();   // composants achetés et quantité totale pour 1 unité

    // F17 - un nœud de l'arbre : l'article, sa quantité cumulée pour 1 unité du sommet, et ses composants
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Noeud {
        private String reference;
        private String libelle;
        private TypeArticle type;
        private BigDecimal quantiteCumulee;
        private int niveau;
        private List<Noeud> composants = new ArrayList<>();
    }

    // F17 - un composant achetable et sa quantité totale
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Besoin {
        private String reference;
        private String libelle;
        private BigDecimal quantiteTotale;
    }
}
