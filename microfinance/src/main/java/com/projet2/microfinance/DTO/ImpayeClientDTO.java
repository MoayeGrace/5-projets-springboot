package com.projet2.microfinance.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// F16 / F18 - impayés d'un client : montant dû et plus grand retard
@Getter
@Setter
@NoArgsConstructor
public class ImpayeClientDTO {

    private Long clientId;
    private String clientNom;
    private BigDecimal montantDu = BigDecimal.ZERO;   // somme des restes à payer des échéances en retard
    private long joursRetardMax;                      // retard de l'échéance impayée la plus ancienne
}
