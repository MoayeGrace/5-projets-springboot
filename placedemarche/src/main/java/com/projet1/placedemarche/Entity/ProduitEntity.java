package com.projet1.placedemarche.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produit")
@Getter
@Setter
@NoArgsConstructor
public class ProduitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prix;

    @Column(nullable = false)
    private Integer stock = 0;

    @ManyToOne
    @JoinColumn(name = "vendeur_id", nullable = false)
    private VendeurEntity vendeur;

    @ManyToOne
    @JoinColumn(name = "categorie_id", nullable = false)
    private CategorieEntity categorie;

    @Column(nullable = false)
    private Boolean alerteStockEnvoyee = false;

    @OneToMany(mappedBy = "produit")
    private List<CommandeEntity> commandes = new ArrayList<>();
}
