package com.projet2.microfinance.DTO;

import com.projet2.microfinance.Entity.TypeCompte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CompteRequestDTO {

    @NotBlank(message = "Le numéro de compte est obligatoire")
    @Size(max = 30)
    private String numero;

    @NotNull(message = "Le type de compte est obligatoire")
    private TypeCompte type;

    @NotNull(message = "La date d'ouverture est obligatoire")
    private LocalDate dateOuverture;

    @NotNull(message = "Le solde est obligatoire")
    private BigDecimal solde;

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;
}
