package com.projet5.lapaie.DTO;

import com.projet5.lapaie.Entity.TypeRubrique;
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
    private String code;
    private String libelle;
    private TypeRubrique type;
    private BigDecimal base;
    private BigDecimal taux;
    private BigDecimal montant;
}
