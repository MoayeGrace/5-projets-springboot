package com.projet4.transporturbain.DTO;

import jakarta.validation.constraints.NotBlank;
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
}