package com.projet3.atelierdeproduction.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MachineResponseDTO {

    private Long id;
    private String code;
    private String type;
    private String atelier;
    private Integer heuresOuvrablesParJour;   // F19
    private LocalDateTime debutMaintenance;   // F12 / F19
    private LocalDateTime finMaintenance;
}
