package com.projet5.lapaie.DTO;

import com.projet5.lapaie.Entity.TypeLigne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// F13 - une ligne du bulletin telle qu'elle est renvoyée
@Getter
@Setter
@NoArgsConstructor
public class LigneBulletinDTO {

    private Integer ordre;
    private TypeLigne type;
    private String rubriqueCode;   // null pour une absence ou une régularisation
    private String libelle;
    private BigDecimal base;
    private BigDecimal taux;
    private BigDecimal montant;
    private String moisCorrige;    // F15 : bulletin corrigé (même employé, ce mois)
    private String motif;          // F15
}
