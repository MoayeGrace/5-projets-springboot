package com.projet5.lapaie.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "absence")
@Getter
@Setter
@NoArgsConstructor
public class AbsenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = false)
    private EmployeEntity employe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeAbsence type;

    // Premier et dernier jour d'absence (inclus)
    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Column(length = 300)
    private String motif;

    // F13 - une absence non rémunérée réduit le brut au prorata des jours
    @Column(nullable = false)
    private Boolean remuneree = true;

    // F12 - vrai si l'absence a au moins un jour en commun avec la période [debut, fin] (bornes incluses)
    public boolean chevauche(LocalDate debut, LocalDate fin) {
        return !dateDebut.isAfter(fin) && !dateFin.isBefore(debut);
    }

    // F8 - durée en jours calendaires, premier et dernier jour inclus
    public long dureeJours() {
        return ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
    }
}