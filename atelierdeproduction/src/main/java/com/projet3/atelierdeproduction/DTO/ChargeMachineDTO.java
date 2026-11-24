package com.projet3.atelierdeproduction.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// F19 - charge d'une machine sur une période : taux d'utilisation, nombre d'ordres et détail jour par jour
@Getter
@Setter
@NoArgsConstructor
public class ChargeMachineDTO {

    private Long machineId;
    private String machineCode;
    private LocalDate du;
    private LocalDate au;
    private BigDecimal heuresDisponibles = BigDecimal.ZERO;   // heures ouvrables moins les arrêts de maintenance
    private BigDecimal heuresOccupees = BigDecimal.ZERO;      // heures occupées par les ordres (hors annulés)
    private BigDecimal tauxUtilisation = BigDecimal.ZERO;     // occupées / disponibles, en %
    private int nombreOrdres;
    private List<Jour> jours = new ArrayList<>();

    // F19 - charge d'une journée
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Jour {
        private LocalDate date;
        private BigDecimal heuresDisponibles;
        private BigDecimal heuresOccupees;
        private BigDecimal tauxUtilisation;
    }
}
