package com.projet5.lapaie.Entity;

// F13 - montant sur lequel s'applique le taux d'une rubrique : le salaire de base du contrat, ou le brut déjà calculé
// par les rubriques précédentes (une rubrique peut ainsi dépendre des précédentes)
public enum Assiette {
    SALAIRE_BASE, BRUT
}
