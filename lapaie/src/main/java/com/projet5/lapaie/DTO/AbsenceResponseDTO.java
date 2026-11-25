package com.projet5.lapaie.DTO;

import com.projet5.lapaie.Entity.TypeAbsence;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class AbsenceResponseDTO {

    private Long id;

    private Long employeId;
    private String employeNom;

    private TypeAbsence type;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String motif;
    private long dureeJours;   // F8 : durée en jours, bornes incluses
}
