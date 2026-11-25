package com.projet5.lapaie.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// F15 - correction d'un bulletin édité, portée sur le bulletin non édité suivant (sans toucher au bulletin d'origine)
@Entity
@Table(name = "regularisation")
@Getter
@Setter
@NoArgsConstructor
public class RegularisationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bulletin corrigé (édité, donc inchangé)
    @ManyToOne
    @JoinColumn(name = "bulletin_origine_id", nullable = false)
    private BulletinEntity bulletinOrigine;

    // Bulletin qui porte la ligne de régularisation ; null tant qu'il n'existe pas encore (en attente)
    @ManyToOne
    @JoinColumn(name = "bulletin_cible_id")
    private BulletinEntity bulletinCible;

    // Montant à ajouter (positif) ou à retirer (négatif)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false, length = 255)
    private String motif;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
}
