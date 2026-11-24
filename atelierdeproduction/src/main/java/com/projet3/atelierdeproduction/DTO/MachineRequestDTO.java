package com.projet3.atelierdeproduction.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MachineRequestDTO {

    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 30)
    private String code;

    @NotBlank(message = "Le type est obligatoire")
    @Size(max = 100)
    private String type;

    @NotBlank(message = "L'atelier est obligatoire")
    @Size(max = 100)
    private String atelier;
}
