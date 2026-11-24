package com.projet2.microfinance.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// F17 - engagement total d'un client sur ses prêts en cours : les totaux et le détail par prêt
@Getter
@Setter
@NoArgsConstructor
public class EncoursDTO {

    private Long clientId;
    private String clientNom;
    private Montants total = new Montants();
    private List<Montants> prets = new ArrayList<>();

    // F17 - les quatre montants agrégés (pour un prêt, ou au total quand pretId est null)
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Montants {
        private Long pretId;
        private BigDecimal capitalRestantDu = BigDecimal.ZERO;
        private BigDecimal interetsAVenir = BigDecimal.ZERO;
        private BigDecimal penalitesDues = BigDecimal.ZERO;
        private BigDecimal montantRembourse = BigDecimal.ZERO;

        // F17 - ajoute les montants d'une autre ligne à celle-ci
        public void ajouter(Montants autre) {
            capitalRestantDu = capitalRestantDu.add(autre.capitalRestantDu);
            interetsAVenir = interetsAVenir.add(autre.interetsAVenir);
            penalitesDues = penalitesDues.add(autre.penalitesDues);
            montantRembourse = montantRembourse.add(autre.montantRembourse);
        }
    }
}
