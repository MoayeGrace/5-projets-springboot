package com.projet5.lapaie.DTO;

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
public class EmployeRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150)
    private String nom;

    @NotBlank(message = "Le matricule est obligatoire")
    @Size(max = 20, message = "Le matricule fait au plus 20 caractères")
    private String matricule;

    @NotNull(message = "La date d'embauche est obligatoire")
    private LocalDate dateEmbauche;

    @NotBlank(message = "Le service est obligatoire")
    @Size(max = 50, message = "Le service fait au plus 50 caractères")
    private String service;
}
