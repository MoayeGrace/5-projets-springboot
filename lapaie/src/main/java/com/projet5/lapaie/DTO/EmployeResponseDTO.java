package com.projet5.lapaie.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EmployeResponseDTO {

    private Long id;
    private String nom;
    private String matricule;
    private LocalDate dateEmbauche;
    private String service;
}
