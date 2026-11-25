package com.projet2.microfinance.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "echeance")
@Getter
@Setter
@NoArgsConstructor
public class EcheanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pret_id", nullable = false)
    private PretEntity pret;

    @Column(nullable = false)
    private LocalDate dateEcheance;

    // Part de capital de l'échéance
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal capital;

    // Part d'intérêts de l'échéance
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal interets;

    // Capital + intérêts, calculé par le service
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    // F14 - pénalités de retard ajoutées au dû
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal penalites = BigDecimal.ZERO;

    // F13 - ce qui a déjà été remboursé sur cette échéance, poste par poste
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal capitalPaye = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal interetsPaye = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal penalitesPaye = BigDecimal.ZERO;

    // Rang de l'échéance dans l'échéancier du prêt (1, 2, 3...), déduit de l'ordre des dates
    public int numero() {
        return pret.getEcheances().indexOf(this) + 1;
    }

    // Total déjà payé sur cette échéance (pénalités comprises)
    public BigDecimal montantPaye() {
        return capitalPaye.add(interetsPaye).add(penalitesPaye);
    }

    // Ce qu'il reste à payer sur cette échéance : capital + intérêts + pénalités, moins ce qui est déjà payé
    public BigDecimal resteAPayer() {
        return montantTotal.add(penalites).subtract(montantPaye());
    }

    // F13 - pénalités restant dues
    public BigDecimal penalitesDues() {
        return penalites.subtract(penalitesPaye);
    }

    // F13 - intérêts restant dus
    public BigDecimal interetsDus() {
        return interets.subtract(interetsPaye);
    }

    // F13 - capital restant dû
    public BigDecimal capitalDu() {
        return capital.subtract(capitalPaye);
    }
}
