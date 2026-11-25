package com.projet5.lapaie.DTO;

import com.projet5.lapaie.Entity.Assiette;
import com.projet5.lapaie.Entity.TypeRubrique;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class RubriqueRequestDTO {

    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 30)
    private String code;

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 150)
    private String libelle;

    @NotNull(message = "Le type est obligatoire (GAIN ou RETENUE)")
    private TypeRubrique type;

    @NotNull(message = "Le taux est obligatoire")
    @PositiveOrZero(message = "Le taux ne peut pas être négatif")
    private BigDecimal taux;

    // F13 - rang dans le calcul du bulletin
    @NotNull(message = "L'ordre de calcul est obligatoire")
    @PositiveOrZero(message = "L'ordre de calcul ne peut pas être négatif")
    private Integer ordreCalcul;

    // F13 - SALAIRE_BASE ou BRUT ; facultatif (SALAIRE_BASE si absent)
    private Assiette assiette;
}
