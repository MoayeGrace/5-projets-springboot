package com.projet3.atelierdeproduction.DTO;

import com.projet3.atelierdeproduction.Entity.TypeArticle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ArticleRequestDTO {

    @NotBlank(message = "La référence est obligatoire")
    @Size(max = 30)
    private String reference;

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 150)
    private String libelle;

    @NotBlank(message = "L'unité est obligatoire")
    @Size(max = 20)
    private String unite;

    @NotNull(message = "Le type est obligatoire (ACHETE ou FABRIQUE)")
    private TypeArticle type;

    // F12 - temps machine d'une unité en minutes ; facultatif (sans objet pour un article acheté)
    @Positive(message = "La durée unitaire doit être positive")
    private Integer dureeUnitaireMinutes;

    // F14 - quantité en magasin ; facultative (0 à la création, inchangée à la modification si absente)
    @PositiveOrZero(message = "La quantité en stock ne peut pas être négative")
    private BigDecimal quantiteStock;
}
