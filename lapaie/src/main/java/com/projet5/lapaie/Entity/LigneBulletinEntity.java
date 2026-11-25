package com.projet5.lapaie.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// F13 - une ligne du bulletin : la rubrique appliquée, sa base, son taux (copié, donc figé) et le montant obtenu ;
// F15 - une ligne de régularisation porte le mois du bulletin corrigé et le motif
@Entity
@Table(name = "ligne_bulletin",
        uniqueConstraints = @UniqueConstraint(columnNames = {"bulletin_id", "ordre"}))
@Getter
@Setter
@NoArgsConstructor
public class LigneBulletinEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bulletin_id", nullable = false)
    private BulletinEntity bulletin;

    // Rang de la ligne dans le bulletin (ordre de calcul)
    @Column(nullable = false)
    private Integer ordre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeLigne type;

    // Rubrique appliquée (null pour une ligne d'absence ou de régularisation)
    @ManyToOne
    @JoinColumn(name = "rubrique_id")
    private RubriqueEntity rubrique;

    @Column(nullable = false, length = 150)
    private String libelle;

    // Montant sur lequel le taux est appliqué
    @Column(precision = 12, scale = 2)
    private BigDecimal base;

    // F14 - taux de la rubrique au moment du calcul : il reste figé même si la rubrique change ensuite
    @Column(precision = 7, scale = 4)
    private BigDecimal taux;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    // F15 - mois du bulletin corrigé (même employé) et motif, pour une régularisation uniquement
    @Column(length = 7)
    private String moisCorrige;

    @Column(length = 300)
    private String motif;
}
