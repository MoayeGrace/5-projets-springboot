package com.projet4.transporturbain.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LigneRequestDTO {

    @NotBlank(message = "Le numéro de ligne est obligatoire")
    @Size(max = 10)
    private String numero;

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 150)
    private String libelle;

    @NotBlank(message = "Le terminus de départ est obligatoire")
    @Size(max = 150)
    private String terminusDepart;

    @NotBlank(message = "Le terminus d'arrivée est obligatoire")
    @Size(max = 150)
    private String terminusArrivee;

    // F12 - durée du trajet en minutes
    @NotNull(message = "La durée du trajet est obligatoire")
    @Positive(message = "La durée du trajet doit être positive")
    private Integer dureeTrajetMinutes;

    // F12 - temps de retour au terminus en minutes ; facultatif (0 si absent)
    @PositiveOrZero(message = "Le temps de retour ne peut pas être négatif")
    private Integer tempsRetourMinutes;
}