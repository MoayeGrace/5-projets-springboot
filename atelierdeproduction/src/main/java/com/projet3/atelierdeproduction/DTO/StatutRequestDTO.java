package com.projet3.atelierdeproduction.DTO;

import com.projet3.atelierdeproduction.Entity.StatutOrdre;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// F13 - corps de POST /ordres-fabrication/{id}/statut : { "statut": "LANCE" }
@Getter
@Setter
@NoArgsConstructor
public class StatutRequestDTO {

    @NotNull(message = "Le nouveau statut est obligatoire")
    private StatutOrdre statut;
}
