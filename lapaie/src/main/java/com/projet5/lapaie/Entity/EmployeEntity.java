package com.projet5.lapaie.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employe")
@Getter
@Setter
@NoArgsConstructor
public class EmployeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, unique = true, length = 20)
    private String matricule;

    @Column(nullable = false)
    private LocalDate dateEmbauche;

    // Service de rattachement (ex : TECHNIQUE, COMMERCIAL)
    @Column(nullable = false, length = 50)
    private String service;

    @OneToMany(mappedBy = "employe")
    private List<ContratEntity> contrats = new ArrayList<>();

    @OneToMany(mappedBy = "employe")
    private List<BulletinEntity> bulletins = new ArrayList<>();

    @OneToMany(mappedBy = "employe")
    private List<AbsenceEntity> absences = new ArrayList<>();
}