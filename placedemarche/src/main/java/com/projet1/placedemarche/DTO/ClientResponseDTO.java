package com.projet1.placedemarche.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClientResponseDTO {

    private Long id;
    private String nom;
    private String email;
    private String adresseLivraison;
}
