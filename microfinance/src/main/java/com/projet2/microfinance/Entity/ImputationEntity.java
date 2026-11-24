package com.projet2.microfinance.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// F13 - part d'un paiement affectée à une échéance : combien a servi aux pénalités, aux intérêts et au capital
@Entity
@Table(name = "imputation")
@Getter
@Setter
@NoArgsConstructor
public class ImputationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paiement_id", nullable = false)
    private PaiementEntity paiement;

    @ManyToOne
    @JoinColumn(name = "echeance_id", nullable = false)
    private EcheanceEntity echeance;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal penalites = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal interets = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal capital = BigDecimal.ZERO;

    // Montant total imputé sur l'échéance
    public BigDecimal total() {
        return penalites.add(interets).add(capital);
    }
}
