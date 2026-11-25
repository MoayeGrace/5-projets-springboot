package com.projet3.atelierdeproduction.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MachineRequestDTO {

    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 30)
    private String code;

    @NotBlank(message = "Le type est obligatoire")
    @Size(max = 50)
    private String type;

    @NotBlank(message = "L'atelier est obligatoire")
    @Size(max = 50)
    private String atelier;

    // F19 - heures ouvrables par jour ; facultatif (8 si absent)
    @Min(value = 1, message = "Une machine travaille au moins 1 heure par jour")
    @Max(value = 24, message = "Une journée compte au plus 24 heures")
    private Integer heuresOuvrablesParJour;

    // F12 / F19 - arrêt de maintenance prévu ; facultatif (les deux dates ensemble)
    private LocalDateTime debutMaintenance;
    private LocalDateTime finMaintenance;
}
