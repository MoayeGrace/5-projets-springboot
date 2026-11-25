package com.projet5.lapaie.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// F17 - masse salariale d'un mois : total général et détail par service
@Getter
@Setter
@NoArgsConstructor
public class MasseSalarialeDTO {

    private String mois;
    private Totaux total = new Totaux();
    private List<Totaux> services = new ArrayList<>();

    // F17 - totaux d'un service (ou de toute l'entreprise quand service est null)
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Totaux {
        private String service;
        private BigDecimal brut = BigDecimal.ZERO;
        private BigDecimal retenues = BigDecimal.ZERO;
        private BigDecimal net = BigDecimal.ZERO;
        private int effectif;                 // nombre de bulletins (un par employé)
        private int bulletinsProvisoires;     // bulletins non encore édités
        private boolean provisoire;           // vrai s'il reste au moins un bulletin non édité
    }
}
