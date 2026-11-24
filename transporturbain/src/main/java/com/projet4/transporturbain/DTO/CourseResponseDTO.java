package com.projet4.transporturbain.DTO;

import com.projet4.transporturbain.Entity.StatutCourse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class CourseResponseDTO {

    private Long id;

    private Long ligneId;
    private String ligneNumero;

    private Long busId;
    private String busImmatriculation;

    private Long conducteurId;
    private String conducteurNom;

    private LocalDate dateCourse;
    private LocalTime heureDepart;
    private StatutCourse statut;
    private LocalTime heureArriveePrevue;   // F15 : départ + durée du trajet
    private LocalTime heureArriveeReelle;   // F15 : renseignée au passage à TERMINEE
}