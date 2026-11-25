package com.projet2.microfinance.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "paiement")
@Getter
@Setter
@NoArgsConstructor
public class PaiementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pret_id", nullable = false)
    private PretEntity pret;

    @Column(nullable = false)
    private LocalDate datePaiement;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModeReglement modeReglement;

    // F13 - imputation retenue : part des pénalités, puis des intérêts, puis du capital (leur somme = montant)
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal partPenalites = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal partInterets = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal partCapital = BigDecimal.ZERO;
}
