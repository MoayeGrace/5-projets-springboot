package com.projet1.placedemarche.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendeur")
@Getter
@Setter
@NoArgsConstructor
public class VendeurEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String raisonSociale;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String ville;

    @OneToMany(mappedBy = "vendeur")
    private List<ProduitEntity> produits = new ArrayList<>();
}
