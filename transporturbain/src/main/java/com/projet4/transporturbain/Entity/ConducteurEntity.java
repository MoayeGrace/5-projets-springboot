package com.projet4.transporturbain.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(nullable = false, unique = true, length = 30)
    private String matricule;

    @Column(nullable = false, unique = true, length = 30)
    private String numeroPermis;

    @OneToMany(mappedBy = "conducteur")
    private List<CourseEntity> courses = new ArrayList<>();
}