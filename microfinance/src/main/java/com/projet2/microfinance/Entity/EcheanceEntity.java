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

    @ManyToOne
    @JoinColumn(name = "pret_id", nullable = false)
    private PretEntity pret;

    // Total déjà payé sur cette échéance
    public BigDecimal montantPaye() {
        return capitalPaye.add(interetsPayes);
    }

    // Ce qu'il reste à payer sur cette échéance
    public BigDecimal resteAPayer() {
        return montantTotal.subtract(montantPaye());
    }
}
