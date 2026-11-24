package com.projet2.microfinance.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// F13 - une ligne du détail de l'imputation d'un paiement
@Getter
@Setter
@NoArgsConstructor
public class ImputationDTO {

    private Long echeanceId;
    private Integer echeanceNumero;
    private BigDecimal penalites;   // part du paiement affectée aux pénalités
    private BigDecimal interets;    // part affectée aux intérêts
    private BigDecimal capital;     // part affectée au capital
}
