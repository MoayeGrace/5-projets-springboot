package com.projet5.lapaie.DTO;

import com.projet5.lapaie.Entity.TypeAbsence;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class AbsenceRequestDTO {

    @NotNull(message = "L'employé est obligatoire")
    private Long employeId;

    @NotNull(message = "Le type d'absence est obligatoire")
    private TypeAbsence type;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;

    @Size(max = 300)
    private String motif;

    // F13 - facultatif : par défaut, seule une absence SANS_SOLDE n'est pas rémunérée
    private Boolean remuneree;
}
