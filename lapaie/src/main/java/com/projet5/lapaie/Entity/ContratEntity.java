package com.projet5.lapaie.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contrat")
@Getter
@Setter
@NoArgsConstructor
public class ContratEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = false)
    private EmployeEntity employe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeContrat type;

    @Column(nullable = false)
    private LocalDate dateDebut;

    // Date de fin ; null pour un contrat sans terme (CDI en cours)
    private LocalDate dateFin;

    @Column(nullable = false, length = 150)
    private String poste;

    // Salaire de base mensuel
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salaireBase;

    // F11 - vrai si le contrat recouvre la période [debut, fin] ; une fin absente signifie "sans terme"
    public boolean chevauche(LocalDate debut, LocalDate fin) {
        boolean commenceAvantLaFin = fin == null || !dateDebut.isAfter(fin);
        boolean finitApresLeDebut = dateFin == null || !dateFin.isBefore(debut);
        return commenceAvantLaFin && finitApresLeDebut;
    }

    // F6 - vrai si le contrat couvre la date donnée (commencé, et pas encore terminé)
    public boolean estActifLe(LocalDate date) {
        return !dateDebut.isAfter(date) && (dateFin == null || !dateFin.isBefore(date));
    }
}