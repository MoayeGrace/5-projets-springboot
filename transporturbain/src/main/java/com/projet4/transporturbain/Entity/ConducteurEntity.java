package com.projet4.transporturbain.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conducteur")
@Getter
@Setter
@NoArgsConstructor
public class ConducteurEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, unique = true, length = 20)
    private String matricule;

    @Column(nullable = false, unique = true, length = 30)
    private String numeroPermis;

    // F14 - date jusqu'à laquelle le permis est valide
    @Column(nullable = false)
    private LocalDate dateValiditePermis;

    // F16 - vrai quand l'expiration proche du permis a déjà été signalée (remis à faux au renouvellement)
    @Column(nullable = false)
    private Boolean alertePermisEnvoyee = false;

    @OneToMany(mappedBy = "conducteur")
    private List<CourseEntity> courses = new ArrayList<>();
}