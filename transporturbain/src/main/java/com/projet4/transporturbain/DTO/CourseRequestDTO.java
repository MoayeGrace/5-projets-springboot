package com.projet4.transporturbain.DTO;

import com.projet4.transporturbain.Entity.StatutCourse;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
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

    // Facultatif : PREVUE à la création si absent, inchangé à la modification si absent
    private StatutCourse statut;
}