package com.projet4.transporturbain.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
// F15 - pas de statut ici : une course est créée PREVUE et son statut ne change que par POST /courses/{id}/statut
public class CourseRequestDTO {

    @NotNull(message = "La ligne est obligatoire")
    private Long ligneId;

    @NotNull(message = "Le bus est obligatoire")
    private Long busId;

    @NotNull(message = "Le conducteur est obligatoire")
    private Long conducteurId;

    @NotNull(message = "La date de la course est obligatoire")
    private LocalDate dateCourse;

    @NotNull(message = "L'heure de départ est obligatoire")
    private LocalTime heureDepart;
}