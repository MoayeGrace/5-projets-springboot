package com.projet2.microfinance.DTO;

import com.projet2.microfinance.Entity.StatutPret;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PretResponseDTO {

    private Long id;
    private BigDecimal montant;
    private Integer dureeMois;
    private BigDecimal tauxAnnuel;
    private LocalDate dateAccord;
    private StatutPret statut;
    private String agent;

    private Long clientId;
    private String clientNom;
}
