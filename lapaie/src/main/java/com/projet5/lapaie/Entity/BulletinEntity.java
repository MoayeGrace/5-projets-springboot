package com.projet5.lapaie.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "bulletin",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employe_id", "mois"}))
@Getter
@Setter
@NoArgsConstructor
public class BulletinEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = false)
    private EmployeEntity employe;

    // Mois de paie au format AAAA-MM (ex : 2026-11)
    @Column(nullable = false, length = 7)
    private String mois;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal brut;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal net;

    // Vrai une fois le bulletin édité (remis à l'employé) : il n'est plus modifiable
    @Column(nullable = false)
    private Boolean edite = false;
}