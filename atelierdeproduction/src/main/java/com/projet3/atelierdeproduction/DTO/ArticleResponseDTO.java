package com.projet3.atelierdeproduction.DTO;

import com.projet3.atelierdeproduction.Entity.TypeArticle;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ArticleResponseDTO {

    private Long id;
    private String reference;
    private String libelle;
    private String unite;
    private TypeArticle type;
    private Integer tempsUnitaireMinutes;   // F12
    private BigDecimal stock;               // F14
}
