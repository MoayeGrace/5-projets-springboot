package com.projet4.transporturbain.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ligne")
@Getter
@Setter
@NoArgsConstructor
public class LigneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Numéro commercial de la ligne (ex : "12", "N3")
    @Column(nullable = false, unique = true, length = 10)
    private String numero;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(nullable = false, length = 150)
    private String terminusDepart;

    @Column(nullable = false, length = 150)
    private String terminusArrivee;

    @OneToMany(mappedBy = "ligne")
    private List<CourseEntity> courses = new ArrayList<>();
}