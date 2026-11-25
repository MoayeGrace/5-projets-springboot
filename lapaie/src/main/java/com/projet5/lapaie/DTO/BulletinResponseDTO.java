package com.projet5.lapaie.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BulletinResponseDTO {

    private Long id;

    private Long employeId;
    private String employeNom;

    private String mois;
    private BigDecimal brut;
    private BigDecimal net;
    private BigDecimal retenues;              // F13
    private boolean edite;
    private LocalDateTime dateEdition;        // F14
    private List<LigneBulletinDTO> lignes;    // F13 : détail du calcul, ligne par ligne
}
