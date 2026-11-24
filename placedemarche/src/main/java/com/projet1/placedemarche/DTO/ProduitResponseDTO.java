package com.projet1.placedemarche.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProduitResponseDTO {

    private Long id;
    private String nom;
    private String description;
    private BigDecimal prix;
    private Integer stock;

    private Long vendeurId;
    private String vendeurRaisonSociale;

    private Long categorieId;
    private String categorieLibelle;
}
