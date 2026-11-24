package com.projet2.microfinance.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// F8 - échéancier d'un prêt : les échéances, et un indicateur qui signale s'il a déjà été généré
@Getter
@Setter
@NoArgsConstructor
public class EcheancierDTO {

    private Long pretId;
    private boolean genere;                        // false si le prêt n'a encore aucune échéance
    private String message;                        // explication quand l'échéancier n'est pas généré
    private List<EcheanceResponseDTO> echeances;   // triées par date croissante
}
