package com.projet2.microfinance.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// F16 - alerte destinée à un agent de crédit : ses clients en retard de paiement
@Getter
@Setter
@NoArgsConstructor
public class AlerteImpayeDTO {

    private String agent;
    private List<ImpayeClientDTO> clients = new ArrayList<>();
}
