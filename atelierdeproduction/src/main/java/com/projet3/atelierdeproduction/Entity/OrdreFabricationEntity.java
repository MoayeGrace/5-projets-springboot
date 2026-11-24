package com.projet3.atelierdeproduction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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

    // Nombre d'unités à produire
    @Column(nullable = false)
    private Integer quantite;

    // Date et heure prévues du début de la production
    @Column(nullable = false)
    private LocalDateTime datePrevue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutOrdre statut = StatutOrdre.PLANIFIE;

    @ManyToOne
    @JoinColumn(name = "machine_id", nullable = false)
    private MachineEntity machine;

    @ManyToOne
    @JoinColumn(name = "operateur_id", nullable = false)
    private OperateurEntity operateur;
}
