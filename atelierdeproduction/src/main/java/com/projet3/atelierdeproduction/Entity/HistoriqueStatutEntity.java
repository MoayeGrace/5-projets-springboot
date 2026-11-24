package com.projet3.atelierdeproduction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// F13 - "chaque changement est horodaté et conservé" : une ligne par changement de statut d'un ordre
@Entity
@Table(name = "historique_statut")
@Getter
@Setter
@NoArgsConstructor
public class HistoriqueStatutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ordre_id", nullable = false)
    private OrdreFabricationEntity ordre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutOrdre ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutOrdre nouveauStatut;

    // F13 - horodatage automatique au moment de l'enregistrement
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateChangement;
}
