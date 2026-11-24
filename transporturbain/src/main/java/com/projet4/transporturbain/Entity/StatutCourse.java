package com.projet4.transporturbain.Entity;

import java.util.EnumSet;
import java.util.Set;

// Cycle de vie d'une course : prévue, en cours, terminée, ou annulée
public enum StatutCourse {
    PREVUE, EN_COURS, TERMINEE, ANNULEE;

    // F15 - statuts atteignables : PREVUE -> EN_COURS -> TERMINEE, ANNULEE seulement avant le départ (depuis PREVUE)
    public Set<StatutCourse> statutsAtteignables() {
        return switch (this) {
            case PREVUE -> EnumSet.of(EN_COURS, ANNULEE);
            case EN_COURS -> EnumSet.of(TERMINEE);
            case TERMINEE, ANNULEE -> EnumSet.noneOf(StatutCourse.class);   // statuts finaux
        };
    }
}
