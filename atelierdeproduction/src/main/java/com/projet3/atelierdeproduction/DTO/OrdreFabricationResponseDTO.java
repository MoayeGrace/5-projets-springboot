package com.projet3.atelierdeproduction.DTO;

import com.projet3.atelierdeproduction.Entity.StatutOrdre;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrdreFabricationResponseDTO {

    private Long id;

    private Long articleId;
    private String articleReference;
    private String articleLibelle;
    private BigDecimal quantite;

    private LocalDateTime datePrevue;
    private LocalDateTime dateFinPrevue;   // F12 : fin du créneau occupé sur la machine
    private StatutOrdre statut;

    private Long machineId;
    private String machineCode;

    private Long operateurId;
    private String operateurNom;

    private Integer versionNomenclature;    // F15 : version utilisée depuis le lancement
    private Integer retardJours;            // F16 : retard cumulé en jours
    private LocalDateTime dateModification; // F13 : horodatage du dernier changement
}
