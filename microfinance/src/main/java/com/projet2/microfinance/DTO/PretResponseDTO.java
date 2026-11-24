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
    private BigDecimal capitalRestantDu;   // F7 : capital qu'il reste à rembourser
    private boolean solde;                 // F7 : true si le prêt est soldé (distingué des prêts en cours)
    private LocalDate dateSolde;           // F15 : date du remboursement complet
    private Integer joursRetardMax;        // F16 : retard maximal atteint, en jours

    private Long clientId;
    private String clientNom;
}
