package com.projet4.transporturbain.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LigneResponseDTO {

    private Long id;
    private String numero;
    private String libelle;
    private String terminusDepart;
    private String terminusArrivee;
    private Integer dureeTrajetMinutes;   // F12
    private Integer tempsRetourMinutes;   // F12
}