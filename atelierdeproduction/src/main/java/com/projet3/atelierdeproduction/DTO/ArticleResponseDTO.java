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
    private Integer dureeUnitaireMinutes;     // F12
    private BigDecimal quantiteStock;         // F14
    private BigDecimal quantiteReservee;      // F14 : réservée par les ordres lancés
    private BigDecimal quantiteDisponible;    // F14 : stock - réservée
}
