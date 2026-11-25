package com.projet5.lapaie.DTO;

import com.projet5.lapaie.Entity.TypeContrat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ContratResponseDTO {

    private Long id;

    private Long employeId;
    private String employeNom;

    private TypeContrat type;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String poste;
    private BigDecimal salaireBase;
    private boolean actif;   // F6 : true pour le contrat en vigueur aujourd'hui
}
