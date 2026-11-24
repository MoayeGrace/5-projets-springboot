package com.projet4.transporturbain.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ArretResponseDTO {

    private Long id;
    private String nom;
    private String commune;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<String> lignes;   // F9 : numéros des lignes qui desservent l'arrêt
}