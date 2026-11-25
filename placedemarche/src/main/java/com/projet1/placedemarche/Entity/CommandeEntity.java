package com.projet1.placedemarche.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "commande")
@Getter
@Setter
@NoArgsConstructor
public class CommandeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false)
    private ProduitEntity produit;

    @Column(nullable = false)
    private Integer quantite;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    // F13 - date de la dernière modification (création ou changement de statut), remise à jour à chaque enregistrement
    @Column(nullable = false)
    private LocalDateTime dateModification;

    // F13 - horodate chaque enregistrement, à la seconde près comme la colonne DATETIME (la réponse = la valeur stockée)
    @PrePersist
    @PreUpdate
    private void horodater() {
        dateModification = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
