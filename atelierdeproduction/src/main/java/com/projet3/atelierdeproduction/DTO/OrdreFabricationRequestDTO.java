package com.projet3.atelierdeproduction.DTO;

import com.projet3.atelierdeproduction.Entity.StatutOrdre;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrdreFabricationRequestDTO {

    @NotNull(message = "L'article est obligatoire")
    private Long articleId;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private Integer quantite;

    @NotNull(message = "La date prévue est obligatoire")
    private LocalDateTime datePrevue;

    // Facultatif : PLANIFIE à la création si absent, inchangé à la modification si absent
    private StatutOrdre statut;

    @NotNull(message = "La machine est obligatoire")
    private Long machineId;

    @NotNull(message = "L'opérateur est obligatoire")
    private Long operateurId;
}
