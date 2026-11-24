package com.projet2.microfinance.DTO;

import com.projet2.microfinance.Entity.StatutPret;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// F19 - tableau de suivi d'un prêt : une ligne par échéance, plus une synthèse
@Getter
@Setter
@NoArgsConstructor
public class SuiviPretDTO {

    private Long pretId;
    private String clientNom;
    private StatutPret statut;
    private BigDecimal montant;

    private BigDecimal totalRembourse;     // somme des paiements reçus
    private BigDecimal capitalRestant;     // capital qu'il reste à rembourser
    private BigDecimal tauxAvancement;     // part du capital déjà remboursée, en %

    private List<Ligne> echeances = new ArrayList<>();

    // F19 - une ligne du tableau : prévu, payé, reste et retard constaté
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Ligne {
        private Integer numero;
        private LocalDate dateEcheance;
        private BigDecimal prevu;          // capital + intérêts + pénalités
        private BigDecimal paye;
        private BigDecimal resteAPayer;
        private long joursRetard;          // retard en cours, ou retard du paiement qui a soldé l'échéance
    }
}
