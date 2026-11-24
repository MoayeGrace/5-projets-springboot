package com.projet4.transporturbain.DTO;

import com.projet4.transporturbain.Entity.SensLigne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// F11 - corps de POST /lignes/{id}/arrets : { "arretId": 3, "sens": "ALLER", "position": 2 }
@Getter
@Setter
@NoArgsConstructor
public class DesserteRequestDTO {

    @NotNull(message = "L'arrêt est obligatoire")
    private Long arretId;

    @NotNull(message = "Le sens est obligatoire (ALLER ou RETOUR)")
    private SensLigne sens;

    @NotNull(message = "La position est obligatoire")
    @Positive(message = "La position commence à 1")
    private Integer position;
}
