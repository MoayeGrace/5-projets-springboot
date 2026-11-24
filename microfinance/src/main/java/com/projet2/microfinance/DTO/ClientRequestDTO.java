package com.projet2.microfinance.DTO;

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
public class ClientRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150)
    private String nom;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(max = 30)
    private String telephone;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 255)
    private String adresse;

    @NotNull(message = "Le plafond d'emprunt est obligatoire")
    @PositiveOrZero(message = "Le plafond d'emprunt ne peut pas être négatif")
    private BigDecimal plafondEmprunt;
}
