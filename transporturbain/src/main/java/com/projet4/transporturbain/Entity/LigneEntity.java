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

    @Column(nullable = false, length = 100)
    private String terminusDepart;

    @Column(nullable = false, length = 100)
    private String terminusArrivee;

    // F12 - durée d'une course d'un terminus à l'autre, en minutes
    @Column(nullable = false)
    private Integer dureeTrajetMinutes = 0;

    // F12 - temps nécessaire au bus pour revenir au terminus et repartir, en minutes
    @Column(nullable = false)
    private Integer tempsRetourMinutes = 0;

    @OneToMany(mappedBy = "ligne")
    private List<CourseEntity> courses = new ArrayList<>();
}