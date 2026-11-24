package com.projet4.transporturbain.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConducteurResponseDTO {

    private Long id;
    private String nom;
    private String matricule;
    private String numeroPermis;
}