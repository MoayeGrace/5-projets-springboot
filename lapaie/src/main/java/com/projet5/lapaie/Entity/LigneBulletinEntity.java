package com.projet5.lapaie.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// F13 - une ligne du bulletin : la rubrique appliquée, sa base, son taux (figé) et le montant obtenu
@Entity
@Table(name = "ligne_bulletin")
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

    // Rang de la ligne dans le bulletin
    @Column(nullable = false)
    private Integer ordre;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 255)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeRubrique type;

    // Montant sur lequel le taux est appliqué (null pour une ligne à montant direct)
    @Column(precision = 12, scale = 2)
    private BigDecimal base;

    // Taux utilisé au moment du calcul : il reste figé même si la rubrique change ensuite
    @Column(precision = 7, scale = 4)
    private BigDecimal taux;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;
}
