package com.projet4.transporturbain.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BusResponseDTO {

    private Long id;
    private String immatriculation;
    private Integer capacite;
    private String depot;
}