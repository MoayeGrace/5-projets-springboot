package com.projet1.placedemarche.Entity;

import java.util.EnumSet;
import java.util.Set;

public enum StatutCommande {
    EN_ATTENTE, PAYEE, EXPEDIEE, LIVREE, ANNULEE;

    // F13 - statuts atteignables depuis ce statut : EN_ATTENTE -> PAYEE -> EXPEDIEE -> LIVREE, ANNULEE depuis EN_ATTENTE ou PAYEE
    public Set<StatutCommande> statutsAtteignables() {
        return switch (this) {
            case EN_ATTENTE -> EnumSet.of(PAYEE, ANNULEE);
            case PAYEE -> EnumSet.of(EXPEDIEE, ANNULEE);
            case EXPEDIEE -> EnumSet.of(LIVREE);
            case LIVREE, ANNULEE -> EnumSet.noneOf(StatutCommande.class);   // statuts finaux
        };
    }
}
