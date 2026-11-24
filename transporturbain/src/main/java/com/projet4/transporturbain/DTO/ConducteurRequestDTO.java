package com.projet4.transporturbain.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

    // F14 - date de fin de validité du permis
    @NotNull(message = "La date de validité du permis est obligatoire")
    private LocalDate dateValiditePermis;
}