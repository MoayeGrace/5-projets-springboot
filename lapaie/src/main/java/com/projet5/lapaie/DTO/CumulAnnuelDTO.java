package com.projet5.lapaie.DTO;

import com.projet5.lapaie.Entity.TypeLigne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// F19 - totaux de l'année d'un employé pour la déclaration : mois par mois, total annuel et détail par rubrique
@Getter
@Setter
@NoArgsConstructor
public class CumulAnnuelDTO {

    private Long employeId;
    private String employeNom;
    private int annee;
    private List<Montants> mois = new ArrayList<>();
    private Montants total = new Montants();
    private List<ParRubrique> rubriques = new ArrayList<>();

    // F19 - brut, retenues et net d'un mois (ou de l'année pour le total)
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Montants {
        private String mois;
        private BigDecimal brut = BigDecimal.ZERO;
        private BigDecimal retenues = BigDecimal.ZERO;
        private BigDecimal net = BigDecimal.ZERO;

        // F19 - ajoute les montants d'une autre ligne à celle-ci
        public void ajouter(BigDecimal brut, BigDecimal retenues, BigDecimal net) {
            this.brut = this.brut.add(brut);
            this.retenues = this.retenues.add(retenues);
            this.net = this.net.add(net);
        }
    }

    // F19 - cumul annuel d'une rubrique (régularisations comprises)
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ParRubrique {
        private String code;
        private String libelle;
        private TypeLigne type;
        private BigDecimal total = BigDecimal.ZERO;
    }
}
