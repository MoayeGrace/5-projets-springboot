package com.projet2.microfinance.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// F14 - résultat du calcul des pénalités de retard d'un prêt à une date donnée
@Getter
@Setter
@NoArgsConstructor
public class PenalitesDTO {

    private Long pretId;
    private LocalDate date;
    private BigDecimal totalPenalites = BigDecimal.ZERO;   // pénalités restant dues sur tout le prêt
    private List<Ligne> echeances = new ArrayList<>();     // une ligne par échéance impayée à la date

    // F14 - une échéance en retard
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Ligne {
        private Long echeanceId;
        private Integer numero;
        private LocalDate dateEcheance;
        private BigDecimal montantDu;     // capital + intérêts restant dus
        private long joursRetard;
        private BigDecimal penalites;     // pénalités de l'échéance après calcul
    }
}
