package com.projet3.atelierdeproduction.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OperateurResponseDTO {

    private Long id;
    private String nom;
    private String matricule;
    private String equipe;
}
