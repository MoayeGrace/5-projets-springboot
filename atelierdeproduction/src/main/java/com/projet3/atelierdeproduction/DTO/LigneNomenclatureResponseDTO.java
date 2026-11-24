package com.projet3.atelierdeproduction.DTO;

import com.projet3.atelierdeproduction.Entity.TypeArticle;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class LigneNomenclatureResponseDTO {

    private Long id;

    private Long composeId;
    private String composeReference;

    private Long composantId;
    private String composantReference;
    private String composantLibelle;
    private TypeArticle composantType;

    private BigDecimal quantite;
}
