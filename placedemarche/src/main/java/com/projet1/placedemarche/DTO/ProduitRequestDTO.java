package com.projet1.placedemarche.DTO;

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
public class ProduitRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150)
    private String nom;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @PositiveOrZero(message = "Le prix ne peut pas être négatif")
    private BigDecimal prix;

    @NotNull(message = "Le stock est obligatoire")
    @PositiveOrZero(message = "Le stock ne peut pas être négatif")
    private Integer stock;

    @NotNull(message = "Le vendeur est obligatoire")
    private Long vendeurId;

    @NotNull(message = "La catégorie est obligatoire")
    private Long categorieId;
}
