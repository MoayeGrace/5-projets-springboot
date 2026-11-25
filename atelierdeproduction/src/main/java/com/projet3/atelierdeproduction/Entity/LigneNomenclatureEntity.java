package com.projet3.atelierdeproduction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

// Une ligne de nomenclature : "pour fabriquer 1 article composé, il faut <quantite> du composant <composantReference>"
// Le composant est désigné par sa référence (pas de seconde clé étrangère vers article : pas de boucle dans le schéma)
@Entity
@Table(name = "ligne_nomenclature")
@Getter
@Setter
@NoArgsConstructor
public class LigneNomenclatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "article_compose_id", nullable = false)
    private ArticleEntity articleCompose;

    @Column(nullable = false, length = 30)
    private String composantReference;

    // Quantité de composant par unité de composé
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantite;

    // F15 - numéro de version de la nomenclature à laquelle appartient la ligne
    @Column(nullable = false)
    private Integer version = 1;

    // F15 - date à partir de laquelle la version s'applique (null = version encore en préparation)
    private LocalDate dateApplication;
}
