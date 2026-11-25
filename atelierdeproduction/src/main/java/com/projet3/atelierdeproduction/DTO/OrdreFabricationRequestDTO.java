package com.projet3.atelierdeproduction.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// F13 - pas de statut ici : un ordre est créé PLANIFIE et son statut ne change que par POST /ordres-fabrication/{id}/statut
@Getter
@Setter
@NoArgsConstructor
public class OrdreFabricationRequestDTO {

    @NotNull(message = "L'article est obligatoire")
    private Long articleId;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private BigDecimal quantite;

    @NotNull(message = "La date prévue est obligatoire")
    private LocalDateTime datePrevue;

    @NotNull(message = "La machine est obligatoire")
    private Long machineId;

    @NotNull(message = "L'opérateur est obligatoire")
    private Long operateurId;
}
