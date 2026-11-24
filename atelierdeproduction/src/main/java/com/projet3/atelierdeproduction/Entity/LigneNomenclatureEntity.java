package com.projet3.atelierdeproduction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// Une ligne de nomenclature : "pour fabriquer 1 composé, il faut <quantite> composant"
@Entity
@Table(name = "ligne_nomenclature",
        uniqueConstraints = @UniqueConstraint(columnNames = {"compose_id", "composant_id"}))
@Getter
@Setter
@NoArgsConstructor
public class LigneNomenclatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compose_id", nullable = false)
    private ArticleEntity compose;

    @ManyToOne
    @JoinColumn(name = "composant_id", nullable = false)
    private ArticleEntity composant;

    // Quantité de composant par unité de composé
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantite;
}
