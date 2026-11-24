package com.projet3.atelierdeproduction.DTO;

import com.projet3.atelierdeproduction.Entity.StatutOrdre;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrdreFabricationResponseDTO {

    private Long id;

    private Long articleId;
    private String articleReference;
    private String articleLibelle;

    private Integer quantite;
    private LocalDateTime datePrevue;
    private LocalDateTime dateFinPrevue;   // F12 : fin du créneau occupé sur la machine
    private StatutOrdre statut;

    private Long machineId;
    private String machineCode;

    private Long operateurId;
    private String operateurNom;

    private List<HistoriqueStatutDTO> historique;   // F13 : changements de statut horodatés
}
