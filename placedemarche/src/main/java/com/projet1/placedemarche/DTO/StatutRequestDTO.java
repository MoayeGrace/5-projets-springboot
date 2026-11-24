package com.projet1.placedemarche.DTO;

import com.projet1.placedemarche.Entity.StatutCommande;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// F13 - corps de POST /commandes/{id}/statut : { "statut": "PAYEE" }
@Getter
@Setter
@NoArgsConstructor
public class StatutRequestDTO {

    @NotNull(message = "Le nouveau statut est obligatoire")
    private StatutCommande statut;
}
