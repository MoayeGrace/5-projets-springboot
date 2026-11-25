package com.projet3.atelierdeproduction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "ordre_fabrication")
@Getter
@Setter
@NoArgsConstructor
public class OrdreFabricationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Article à produire
    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleEntity article;

    // Quantité à produire
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantite;

    @ManyToOne
    @JoinColumn(name = "machine_id", nullable = false)
    private MachineEntity machine;

    @ManyToOne
    @JoinColumn(name = "operateur_id", nullable = false)
    private OperateurEntity operateur;

    // F12 - créneau occupé sur la machine : début prévu, et fin = début + quantité × durée unitaire (calculée par le service)
    @Column(nullable = false)
    private LocalDateTime datePrevue;

    @Column(nullable = false)
    private LocalDateTime dateFinPrevue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutOrdre statut = StatutOrdre.PLANIFIE;

    // F15 - version de nomenclature utilisée par l'ordre, fixée au lancement
    private Integer versionNomenclature;

    // F16 - retard cumulé en jours, mis à jour chaque nuit par la détection des ordres en retard
    @Column(nullable = false)
    private Integer retardJours = 0;

    // F13 - date du dernier changement (dont le statut), horodatée à chaque enregistrement
    @Column(nullable = false)
    private LocalDateTime dateModification;

    // F13 - horodate chaque enregistrement, à la seconde près comme la colonne DATETIME (la réponse = la valeur stockée)
    @PrePersist
    @PreUpdate
    private void horodater() {
        dateModification = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    // F12 - vrai si le créneau de cet ordre recouvre l'intervalle [debut, fin[
    public boolean chevauche(LocalDateTime debut, LocalDateTime fin) {
        return datePrevue.isBefore(fin) && dateFinPrevue.isAfter(debut);
    }
}
