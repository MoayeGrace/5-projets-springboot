package com.projet4.transporturbain.DTO;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ArretRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150)
    private String nom;

    @NotBlank(message = "La commune est obligatoire")
    @Size(max = 100)
    private String commune;

    @NotNull(message = "La latitude est obligatoire")
    @DecimalMin(value = "-90", message = "La latitude est comprise entre -90 et 90")
    @DecimalMax(value = "90", message = "La latitude est comprise entre -90 et 90")
    private BigDecimal latitude;

    @NotNull(message = "La longitude est obligatoire")
    @DecimalMin(value = "-180", message = "La longitude est comprise entre -180 et 180")
    @DecimalMax(value = "180", message = "La longitude est comprise entre -180 et 180")
    private BigDecimal longitude;
}