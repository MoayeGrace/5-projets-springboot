package com.projet3.atelierdeproduction.DTO;

import com.projet3.atelierdeproduction.Entity.StatutVersion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// F15 - une version de nomenclature et ses lignes figées
@Getter
@Setter
@NoArgsConstructor
public class VersionNomenclatureDTO {

    private Long id;
    private Long articleId;
    private String articleReference;
    private Integer numero;
    private StatutVersion statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateApplication;
    private List<Ligne> lignes = new ArrayList<>();

    // F15 - une ligne figée : composant et quantité
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Ligne {
        private String composantReference;
        private BigDecimal quantite;
    }
}
