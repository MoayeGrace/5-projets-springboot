package com.projet4.transporturbain.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ligne_id", nullable = false)
    private LigneEntity ligne;

    @ManyToOne
    @JoinColumn(name = "bus_id", nullable = false)
    private BusEntity bus;

    @ManyToOne
    @JoinColumn(name = "conducteur_id", nullable = false)
    private ConducteurEntity conducteur;

    @Column(nullable = false)
    private LocalDate dateCourse;

    @Column(nullable = false)
    private LocalTime heureDepart;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCourse statut = StatutCourse.PREVUE;

    // F12 - date et heure de départ
    public LocalDateTime depart() {
        return dateCourse.atTime(heureDepart);
    }

    // F12 - heure d'arrivée prévue au terminus : départ + durée du trajet de la ligne
    public LocalDateTime arriveePrevue() {
        return depart().plusMinutes(ligne.getDureeTrajetMinutes());
    }

    // F12 - fin d'occupation du bus : arrivée + temps de retour au terminus
    public LocalDateTime finOccupationBus() {
        return arriveePrevue().plusMinutes(ligne.getTempsRetourMinutes());
    }
}