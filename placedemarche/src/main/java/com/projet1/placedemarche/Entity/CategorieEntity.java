package com.projet1.placedemarche.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorie")
@Getter
@Setter
@NoArgsConstructor
public class CategorieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String libelle;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "categorie")
    private List<ProduitEntity> produits = new ArrayList<>();
}
