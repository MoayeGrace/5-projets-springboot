package com.projet2.microfinance.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EcheanceResponseDTO {

    private Long id;
    private Integer numero;
    private LocalDate dateEcheance;
    private BigDecimal montantCapital;
    private BigDecimal montantInterets;
    private BigDecimal montantTotal;
    private BigDecimal montantPaye;
    private BigDecimal resteAPayer;

    private Long pretId;
}
