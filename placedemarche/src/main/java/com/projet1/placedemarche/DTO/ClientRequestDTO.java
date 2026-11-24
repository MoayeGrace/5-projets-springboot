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
public class ClientRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150)
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email n'est pas valide")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "L'adresse de livraison est obligatoire")
    @Size(max = 300)
    private String adresseLivraison;
}
