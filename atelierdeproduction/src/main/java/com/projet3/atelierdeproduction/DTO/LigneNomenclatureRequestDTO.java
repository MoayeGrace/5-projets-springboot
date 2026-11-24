package com.projet3.atelierdeproduction.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class LigneNomenclatureRequestDTO {

    @NotNull(message = "L'article composé est obligatoire")
    private Long composeId;

    @NotNull(message = "Le composant est obligatoire")
    private Long composantId;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private BigDecimal quantite;
}
