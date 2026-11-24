package com.projet1.placedemarche.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VendeurRequestDTO {

    @NotBlank(message = "La raison sociale est obligatoire")
    @Size(max = 150)
    private String raisonSociale;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email n'est pas valide")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100)
    private String ville;
}
