package com.projet5.lapaie.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// F15 - une régularisation : le bulletin corrigé, le montant, le motif et le bulletin qui la porte
@Getter
@Setter
@NoArgsConstructor
public class RegularisationResponseDTO {

    private Long id;
    private Long bulletinOrigineId;
    private String moisOrigine;
    private BigDecimal montant;
    private String motif;
    private Long bulletinCibleId;   // null tant que le bulletin suivant n'existe pas
    private String moisCible;
    private boolean enAttente;
}
