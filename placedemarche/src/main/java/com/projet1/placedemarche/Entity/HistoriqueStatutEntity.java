package com.projet1.placedemarche.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// F13 - "chaque changement est horodaté et conservé" : une ligne par changement de statut d'une commande
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
    @JoinColumn(name = "commande_id", nullable = false)
    private CommandeEntity commande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCommande ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCommande nouveauStatut;

    // F13 - horodatage automatique au moment de l'enregistrement
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateChangement;
}
