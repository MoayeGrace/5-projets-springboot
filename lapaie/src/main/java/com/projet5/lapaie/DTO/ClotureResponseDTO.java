package com.projet5.lapaie.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// F16 - résultat de la clôture d'un mois de paie
@Getter
@Setter
@NoArgsConstructor
public class ClotureResponseDTO {

    private String mois;
    private LocalDateTime dateCloture;
    private int nombreBulletins;   // bulletins édités du mois
}
