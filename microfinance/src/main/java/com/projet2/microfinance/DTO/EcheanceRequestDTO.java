package com.projet2.microfinance.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EcheanceRequestDTO {

    @NotNull(message = "Le numéro de l'échéance est obligatoire")
    @Positive(message = "Le numéro doit être positif")
    private Integer numero;

    @NotNull(message = "La date de l'échéance est obligatoire")
    private LocalDate dateEcheance;

    @NotNull(message = "La part de capital est obligatoire")
    @PositiveOrZero(message = "La part de capital ne peut pas être négative")
    private BigDecimal montantCapital;

    @NotNull(message = "La part d'intérêts est obligatoire")
    @PositiveOrZero(message = "La part d'intérêts ne peut pas être négative")
    private BigDecimal montantInterets;

    @NotNull(message = "Le prêt est obligatoire")
    private Long pretId;
}
