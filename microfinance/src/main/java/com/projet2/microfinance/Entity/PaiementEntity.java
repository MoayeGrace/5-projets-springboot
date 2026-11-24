package com.projet2.microfinance.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "paiement")
@Getter
@Setter
@NoArgsConstructor
public class PaiementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate datePaiement;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModeReglement modeReglement;

    @ManyToOne
    @JoinColumn(name = "pret_id", nullable = false)
    private PretEntity pret;

    // F13 - détail de l'imputation ; cascade ALL : enregistré et supprimé avec le paiement
    @OneToMany(mappedBy = "paiement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImputationEntity> imputations = new ArrayList<>();
}
