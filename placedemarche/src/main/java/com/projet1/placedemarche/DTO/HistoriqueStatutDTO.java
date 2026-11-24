package com.projet1.placedemarche.DTO;

import com.projet1.placedemarche.Entity.StatutCommande;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// F13 - une ligne de l'historique des statuts renvoyée par GET /commandes/{id}/historique
@Getter
@Setter
@NoArgsConstructor
public class HistoriqueStatutDTO {

    private StatutCommande ancienStatut;
    private StatutCommande nouveauStatut;
    private LocalDateTime dateChangement;
}
