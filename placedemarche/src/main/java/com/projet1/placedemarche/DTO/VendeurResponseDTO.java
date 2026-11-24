package com.projet1.placedemarche.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VendeurResponseDTO {

    private Long id;
    private String raisonSociale;
    private String email;
    private String ville;
}
