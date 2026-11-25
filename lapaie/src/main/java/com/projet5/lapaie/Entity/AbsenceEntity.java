package com.projet5.lapaie.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

    @Column(length = 255)
    private String motif;
}