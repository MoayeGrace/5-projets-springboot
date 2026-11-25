package com.projet2.microfinance.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.projet2.microfinance.Entity.ModeReglement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PaiementResponseDTO {

    private Long id;
    private LocalDate datePaiement;
    private BigDecimal montant;
    private ModeReglement modeReglement;

    private Long pretId;

    // F9 / F13 - imputation retenue : parts du montant affectées aux pénalités, aux intérêts et au capital
    private BigDecimal partPenalites;
    private BigDecimal partInterets;
    private BigDecimal partCapital;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ImputationDTO> imputations;   // F13 : détail échéance par échéance, renvoyé à l'enregistrement du paiement
}
