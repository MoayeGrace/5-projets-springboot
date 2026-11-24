package com.projet2.microfinance.DTO;

import com.projet2.microfinance.Entity.ModeReglement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PaiementRequestDTO {

    @NotNull(message = "La date du paiement est obligatoire")
    private LocalDate datePaiement;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotNull(message = "Le mode de règlement est obligatoire")
    private ModeReglement modeReglement;

    @NotNull(message = "Le prêt est obligatoire")
    private Long pretId;
}
