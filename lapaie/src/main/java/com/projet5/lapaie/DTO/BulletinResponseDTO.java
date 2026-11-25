package com.projet5.lapaie.DTO;

import com.projet5.lapaie.Entity.EtatBulletin;
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
    private BigDecimal retenues;              // F13
    private BigDecimal net;

    private EtatBulletin etat;                // F7 / F14 / F16 : BROUILLON, EDITE ou CLOTURE
    private LocalDateTime dateModification;
    private List<LigneBulletinDTO> lignes;    // F13 : détail du calcul, ligne par ligne
}
