package com.projet4.transporturbain.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

// F18 - ponctualité d'une ligne sur un mois : taux global, retard moyen et détail par tranche horaire
@Getter
@Setter
@NoArgsConstructor
public class PonctualiteDTO {

    private Long ligneId;
    private String ligneNumero;
    private YearMonth mois;
    private int nombreCourses;                                 // courses terminées avec une heure réelle d'arrivée
    private int nombrePonctuelles;                             // arrivées au plus 5 minutes après l'horaire prévu
    private BigDecimal tauxPonctualite = BigDecimal.ZERO;      // en %
    private BigDecimal retardMoyenMinutes = BigDecimal.ZERO;   // une arrivée en avance compte 0
    private List<Tranche> tranches = new ArrayList<>();

    // F18 - ponctualité des courses parties dans une même heure (ex : "07h-08h")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Tranche {
        private String tranche;
        private int nombreCourses;
        private int nombrePonctuelles;
        private BigDecimal tauxPonctualite;
    }
}
