package com.projet3.atelierdeproduction.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MachineResponseDTO {

    private Long id;
    private String code;
    private String type;
    private String atelier;
}
