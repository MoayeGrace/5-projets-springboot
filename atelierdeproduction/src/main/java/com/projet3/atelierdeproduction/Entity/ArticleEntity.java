package com.projet3.atelierdeproduction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "article")
@Getter
@Setter
@NoArgsConstructor
public class ArticleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(nullable = false, length = 150)
    private String libelle;

    // Unité de mesure (pièce, kg, m...)
    @Column(nullable = false, length = 20)
    private String unite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeArticle type;

    // F12 - gamme : temps de fabrication d'une unité sur machine, en minutes (0 pour un article acheté)
    @Column(nullable = false)
    private Integer tempsUnitaireMinutes = 0;

    // Lignes de nomenclature dont cet article est le composé : "de quoi il est fait"
    @OneToMany(mappedBy = "compose")
    private List<LigneNomenclatureEntity> composants = new ArrayList<>();

    // Lignes de nomenclature dont cet article est le composant : "où il est utilisé"
    @OneToMany(mappedBy = "composant")
    private List<LigneNomenclatureEntity> utilisations = new ArrayList<>();
}
