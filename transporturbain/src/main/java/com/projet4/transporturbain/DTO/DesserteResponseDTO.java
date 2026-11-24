package com.projet4.transporturbain.DTO;

import com.projet4.transporturbain.Entity.SensLigne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// F6 - un arrêt d'une ligne, avec sa commune et sa position dans l'ordre de desserte
@Getter
@Setter
@NoArgsConstructor
public class DesserteResponseDTO {

    private Long ligneId;
    private SensLigne sens;
    private Integer position;

    private Long arretId;
    private String arretNom;
    private String commune;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
