package com.projet4.transporturbain.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BusRequestDTO {

    @NotBlank(message = "L'immatriculation est obligatoire")
    @Size(max = 20)
    private String immatriculation;

    @NotNull(message = "La capacité est obligatoire")
    @Positive(message = "La capacité doit être positive")
    private Integer capacite;

    @NotBlank(message = "Le dépôt est obligatoire")
    @Size(max = 100)
    private String depot;
}