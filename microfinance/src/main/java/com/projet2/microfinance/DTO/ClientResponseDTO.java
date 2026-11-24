package com.projet2.microfinance.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ClientResponseDTO {

    private Long id;
    private String nom;
    private String telephone;
    private String adresse;
    private BigDecimal plafondEmprunt;
}
