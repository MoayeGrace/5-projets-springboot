package com.projet5.lapaie.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// F15 - corps de POST /bulletins/{id}/regularisation : { "montant": 15000, "motif": "Prime oubliée" }
@Getter
@Setter
@NoArgsConstructor
public class RegularisationRequestDTO {

    @NotNull(message = "Le montant est obligatoire")
    private BigDecimal montant;

    @NotBlank(message = "Le motif est obligatoire")
    @Size(max = 300)
    private String motif;
}
