package com.projet4.transporturbain.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

// F19 - activité d'un bus sur un mois : courses assurées, heures de service, lignes desservies et détail par ligne
@Getter
@Setter
@NoArgsConstructor
public class ActiviteBusDTO {

    private Long busId;
    private String immatriculation;
    private YearMonth mois;
    private int nombreCourses;
    private BigDecimal heuresService = BigDecimal.ZERO;
    private List<String> lignesDesservies = new ArrayList<>();
    private List<ParLigne> detailParLigne = new ArrayList<>();

    // F19 - activité du bus sur une ligne
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ParLigne {
        private String ligneNumero;
        private int nombreCourses;
        private BigDecimal heuresService;
    }
}
