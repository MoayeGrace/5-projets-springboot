package com.projet5.lapaie.DTO;

import com.projet5.lapaie.Entity.TypeContrat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ContratRequestDTO {

    @NotNull(message = "L'employé est obligatoire")
    private Long employeId;

    @NotNull(message = "Le type de contrat est obligatoire")
    private TypeContrat type;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    // Facultative : absente pour un contrat sans terme
    private LocalDate dateFin;

    @NotBlank(message = "Le poste est obligatoire")
    @Size(max = 100, message = "Le poste fait au plus 100 caractères")
    private String poste;

    @NotNull(message = "Le salaire de base est obligatoire")
    @Positive(message = "Le salaire de base doit être positif")
    private BigDecimal salaireBase;
}
