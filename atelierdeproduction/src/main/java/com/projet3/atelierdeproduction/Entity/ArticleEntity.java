package com.projet3.atelierdeproduction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
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

    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @Column(nullable = false, length = 150)
    private String libelle;

    // Unité de mesure (PIECE, KG, METRE...)
    @Column(nullable = false, length = 20)
    private String unite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeArticle type;

    // F12 - temps machine pour produire une unité, en minutes (null pour un article acheté)
    private Integer dureeUnitaireMinutes;

    // F14 - quantité en magasin, et part déjà réservée par les ordres lancés
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantiteStock = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantiteReservee = BigDecimal.ZERO;

    // Lignes de nomenclature dont cet article est le composé (toutes versions confondues)
    @OneToMany(mappedBy = "articleCompose")
    private List<LigneNomenclatureEntity> lignesNomenclature = new ArrayList<>();

    // F14 - quantité réellement disponible : stock moins réservations
    public BigDecimal quantiteDisponible() {
        return quantiteStock.subtract(quantiteReservee);
    }
}
