package com.projet4.transporturbain.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// F16 - alerte pour un permis proche de l'expiration, avec les courses déjà programmées au-delà
@Getter
@Setter
@NoArgsConstructor
public class AlertePermisDTO {

    private Long conducteurId;
    private String conducteurNom;
    private LocalDate dateExpiration;
    private List<CourseProgrammee> coursesApresExpiration = new ArrayList<>();

    // F16 - une course programmée après l'expiration du permis
    @Getter
    @Setter
    @NoArgsConstructor
    public static class CourseProgrammee {
        private Long courseId;
        private String ligneNumero;
        private LocalDate dateCourse;
        private LocalTime heureDepart;
    }
}
