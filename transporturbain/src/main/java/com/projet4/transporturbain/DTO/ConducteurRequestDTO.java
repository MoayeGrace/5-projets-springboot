package com.projet4.transporturbain.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConducteurRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150)
    private String nom;

    @NotBlank(message = "Le matricule est obligatoire")
    @Size(max = 30)
    private String matricule;

    @NotBlank(message = "Le numéro de permis est obligatoire")
    @Size(max = 30)
    private String numeroPermis;
}