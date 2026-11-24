package com.projet3.atelierdeproduction.DTO;

import com.projet3.atelierdeproduction.Entity.StatutOrdre;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// F16 - alerte d'un atelier : ses ordres en retard
@Getter
@Setter
@NoArgsConstructor
public class AlerteRetardDTO {

    private String atelier;
    private List<Ligne> ordres = new ArrayList<>();

    // F16 - un ordre en retard
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Ligne {
        private Long ordreId;
        private String articleReference;
        private long retardJours;
        private StatutOrdre statut;
    }
}
