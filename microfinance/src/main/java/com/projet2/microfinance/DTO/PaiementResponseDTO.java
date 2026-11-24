package com.projet2.microfinance.DTO;

import com.projet2.microfinance.Entity.ModeReglement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PaiementResponseDTO {

    private Long id;
    private LocalDate datePaiement;
    private BigDecimal montant;
    private ModeReglement modeReglement;

    private Long pretId;
}
