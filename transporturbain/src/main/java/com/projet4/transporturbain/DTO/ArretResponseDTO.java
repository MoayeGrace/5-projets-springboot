package com.projet4.transporturbain.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ArretResponseDTO {

    private Long id;
    private String nom;
    private String commune;
    private BigDecimal latitude;
    private BigDecimal longitude;
}