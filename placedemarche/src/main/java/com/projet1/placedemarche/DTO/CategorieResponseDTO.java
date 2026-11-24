package com.projet1.placedemarche.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategorieResponseDTO {

    private Long id;
    private String libelle;
    private String description;
}
