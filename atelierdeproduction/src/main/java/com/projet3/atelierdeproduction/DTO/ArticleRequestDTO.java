package com.projet3.atelierdeproduction.DTO;

import com.projet3.atelierdeproduction.Entity.TypeArticle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ArticleRequestDTO {

    @NotBlank(message = "La référence est obligatoire")
    @Size(max = 50)
    private String reference;

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 150)
    private String libelle;

    @NotBlank(message = "L'unité est obligatoire")
    @Size(max = 20)
    private String unite;

    @NotNull(message = "Le type est obligatoire (ACHETE ou FABRIQUE)")
    private TypeArticle type;

    // F12 - temps de fabrication d'une unité en minutes ; facultatif (0 si absent)
    @PositiveOrZero(message = "Le temps unitaire ne peut pas être négatif")
    private Integer tempsUnitaireMinutes;
}
