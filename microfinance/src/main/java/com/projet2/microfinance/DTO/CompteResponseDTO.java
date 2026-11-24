package com.projet2.microfinance.DTO;

import com.projet2.microfinance.Entity.TypeCompte;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CompteResponseDTO {

    private Long id;
    private String numero;
    private TypeCompte type;
    private LocalDate dateOuverture;
    private BigDecimal solde;

    private Long clientId;
    private String clientNom;
}
