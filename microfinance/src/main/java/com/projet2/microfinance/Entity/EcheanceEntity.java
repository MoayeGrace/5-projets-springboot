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

    // Rang de l'échéance dans l'échéancier (1, 2, 3...)
    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false)
    private LocalDate dateEcheance;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montantCapital;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montantInterets;

    // Capital + intérêts, calculé par le service
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    // Part de capital déjà remboursée
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal capitalPaye = BigDecimal.ZERO;

    // Part d'intérêts déjà remboursée
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal interetsPayes = BigDecimal.ZERO;

    // F13 / F14 - pénalités de retard dues sur cette échéance (calculées par F14)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal penalites = BigDecimal.ZERO;

    // F13 - part des pénalités déjà remboursée
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal penalitesPayees = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "pret_id", nullable = false)
    private PretEntity pret;

    // Total déjà payé sur cette échéance (pénalités comprises)
    public BigDecimal montantPaye() {
        return capitalPaye.add(interetsPayes).add(penalitesPayees);
    }

    // Ce qu'il reste à payer sur cette échéance : capital + intérêts + pénalités, moins ce qui est déjà payé
    public BigDecimal resteAPayer() {
        return montantTotal.add(penalites).subtract(montantPaye());
    }

    // F13 - pénalités restant dues
    public BigDecimal penalitesDues() {
        return penalites.subtract(penalitesPayees);
    }

    // F13 - intérêts restant dus
    public BigDecimal interetsDus() {
        return montantInterets.subtract(interetsPayes);
    }

    // F13 - capital restant dû
    public BigDecimal capitalDu() {
        return montantCapital.subtract(capitalPaye);
    }
}
