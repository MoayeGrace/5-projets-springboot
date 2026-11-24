package com.projet2.microfinance.DTO;

import com.projet2.microfinance.Entity.StatutPret;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PretRequestDTO {

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotNull(message = "La durée en mois est obligatoire")
    @Positive(message = "La durée doit être positive")
    private Integer dureeMois;

    @NotNull(message = "Le taux annuel est obligatoire")
    @PositiveOrZero(message = "Le taux ne peut pas être négatif")
    private BigDecimal tauxAnnuel;

    @NotNull(message = "La date d'accord est obligatoire")
    private LocalDate dateAccord;

    // Facultatif : ACCORDE à la création si absent, inchangé à la modification si absent
    private StatutPret statut;

    @NotBlank(message = "L'agent de crédit est obligatoire")
    @Size(max = 100)
    private String agent;

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;
}
