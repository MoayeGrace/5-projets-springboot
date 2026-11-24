package com.projet4.transporturbain.DTO;

import com.projet4.transporturbain.Entity.StatutCourse;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

// F15 - corps de POST /courses/{id}/statut : { "statut": "TERMINEE", "heureArriveeReelle": "07:52" }
@Getter
@Setter
@NoArgsConstructor
public class StatutRequestDTO {

    @NotNull(message = "Le nouveau statut est obligatoire")
    private StatutCourse statut;

    // Facultatif : heure réelle d'arrivée pour TERMINEE ; l'heure actuelle si absente
    private LocalTime heureArriveeReelle;
}
