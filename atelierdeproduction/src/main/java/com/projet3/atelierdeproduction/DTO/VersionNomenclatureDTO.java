package com.projet3.atelierdeproduction.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// F15 - une version de la nomenclature d'un article et ses lignes
@Getter
@Setter
@NoArgsConstructor
public class VersionNomenclatureDTO {

    private Long articleId;
    private String articleReference;
    private Integer numero;
    private boolean enPreparation;
    private LocalDate dateApplication;   // null tant que la version est en préparation
    private List<Ligne> lignes = new ArrayList<>();

    // F15 - une ligne de la version : composant et quantité
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Ligne {
        private String composantReference;
        private BigDecimal quantite;
    }
}
