package com.projet1.placedemarche.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCommande;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;

    // F13 - historique des changements de statut ; cascade ALL : enregistré et supprimé avec la commande
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateChangement ASC")
    private List<HistoriqueStatutEntity> historique = new ArrayList<>();
}
