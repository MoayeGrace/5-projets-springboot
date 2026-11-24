package com.projet3.atelierdeproduction.Entity;

import java.util.EnumSet;
import java.util.Set;

// Cycle de vie d'un ordre de fabrication : planifié, lancé, terminé, ou annulé
public enum StatutOrdre {
    PLANIFIE, LANCE, TERMINE, ANNULE;

    // F13 - statuts atteignables : PLANIFIE -> LANCE -> TERMINE, ANNULE seulement avant TERMINE
    public Set<StatutOrdre> statutsAtteignables() {
        return switch (this) {
            case PLANIFIE -> EnumSet.of(LANCE, ANNULE);
            case LANCE -> EnumSet.of(TERMINE, ANNULE);
            case TERMINE, ANNULE -> EnumSet.noneOf(StatutOrdre.class);   // statuts finaux
        };
    }
}
