package com.projet5.lapaie.DTO;

import com.projet5.lapaie.Entity.TypeRubrique;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class RubriqueResponseDTO {

    private Long id;
    private String code;
    private String libelle;
    private TypeRubrique type;
    private BigDecimal taux;
}
