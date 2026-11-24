package com.projet3.atelierdeproduction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "operateur")
@Getter
@Setter
@NoArgsConstructor
public class OperateurEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, unique = true, length = 30)
    private String matricule;

    @Column(nullable = false, length = 50)
    private String equipe;

    @OneToMany(mappedBy = "operateur")
    private List<OrdreFabricationEntity> ordres = new ArrayList<>();
}
