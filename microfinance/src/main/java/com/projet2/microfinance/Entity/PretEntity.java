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
@Table(name = "pret")
@Getter
@Setter
@NoArgsConstructor
public class PretEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private Integer dureeMois;

    // Taux d'intérêt annuel en pourcentage (ex : 12.00 pour 12 %)
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxAnnuel;

    @Column(nullable = false)
    private LocalDate dateAccord;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutPret statut = StatutPret.ACCORDE;

    // Agent de crédit qui suit le dossier
    @Column(nullable = false, length = 100)
    private String agent;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @OneToMany(mappedBy = "pret")
    @OrderBy("numero ASC")
    private List<EcheanceEntity> echeances = new ArrayList<>();

    @OneToMany(mappedBy = "pret")
    private List<PaiementEntity> paiements = new ArrayList<>();
}
