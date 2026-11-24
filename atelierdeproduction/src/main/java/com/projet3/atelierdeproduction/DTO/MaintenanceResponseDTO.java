package com.projet3.atelierdeproduction.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// F19 - un arrêt de maintenance
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceResponseDTO {

    private Long id;
    private Long machineId;
    private LocalDateTime debut;
    private LocalDateTime fin;
    private String motif;
}
