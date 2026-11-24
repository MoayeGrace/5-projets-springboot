package com.projet3.atelierdeproduction.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// F19 - corps de POST /machines/{id}/maintenances
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceRequestDTO {

    @NotNull(message = "Le début de l'arrêt est obligatoire")
    private LocalDateTime debut;

    @NotNull(message = "La fin de l'arrêt est obligatoire")
    private LocalDateTime fin;

    @Size(max = 255)
    private String motif;
}
