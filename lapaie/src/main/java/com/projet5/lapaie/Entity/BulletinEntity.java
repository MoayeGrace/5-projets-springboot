package com.projet5.lapaie.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private BigDecimal brut = BigDecimal.ZERO;

    // F13 - total des retenues (cotisations)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal retenues = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal net = BigDecimal.ZERO;

    // F14 / F16 - BROUILLON, puis EDITE (figé), puis CLOTURE (mois arrêté)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtatBulletin etat = EtatBulletin.BROUILLON;

    // Date de la dernière modification, horodatée à chaque enregistrement
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;

    // F13 - lignes du calcul ; cascade ALL : enregistrées et supprimées avec le bulletin
    @OneToMany(mappedBy = "bulletin", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    private List<LigneBulletinEntity> lignes = new ArrayList<>();
}
