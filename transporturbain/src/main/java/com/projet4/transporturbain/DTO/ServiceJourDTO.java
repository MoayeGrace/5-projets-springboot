package com.projet4.transporturbain.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// F17 - exploitation d'une journée : les courses groupées par ligne
@Getter
@Setter
@NoArgsConstructor
public class ServiceJourDTO {

    private LocalDate date;
    private List<Ligne> lignes = new ArrayList<>();

    // F17 - le service d'une ligne : ses courses par heure de départ, et le nombre d'annulées
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Ligne {
        private Long ligneId;
        private String ligneNumero;
        private String ligneLibelle;
        private int nombreCourses;
        private int nombreAnnulees;
        private List<CourseResponseDTO> courses = new ArrayList<>();
    }
}
