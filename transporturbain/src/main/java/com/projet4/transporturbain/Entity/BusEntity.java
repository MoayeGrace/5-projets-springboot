package com.projet4.transporturbain.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bus")
@Getter
@Setter
@NoArgsConstructor
public class BusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String immatriculation;

    // Nombre de places
    @Column(nullable = false)
    private Integer capacite;

    // Dépôt où le bus est remisé
    @Column(nullable = false, length = 100)
    private String depot;

    @OneToMany(mappedBy = "bus")
    private List<CourseEntity> courses = new ArrayList<>();
}