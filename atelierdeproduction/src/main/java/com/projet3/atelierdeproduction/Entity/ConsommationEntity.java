package com.projet3.atelierdeproduction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// F14 - quantité d'un composant acheté réservée (sortie du magasin) au lancement d'un ordre
@Entity
@Table(name = "consommation")
@Getter
@Setter
@NoArgsConstructor
public class ConsommationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ordre_id", nullable = false)
    private OrdreFabricationEntity ordre;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleEntity article;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantite;
}
