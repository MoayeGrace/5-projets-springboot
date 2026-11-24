package com.projet3.atelierdeproduction.DTO;

import com.projet3.atelierdeproduction.Entity.StatutOrdre;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// F13 - une ligne de l'historique des statuts d'un ordre
@Getter
@Setter
@NoArgsConstructor
public class HistoriqueStatutDTO {

    private StatutOrdre ancienStatut;
    private StatutOrdre nouveauStatut;
    private LocalDateTime dateChangement;
}
