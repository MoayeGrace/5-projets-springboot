package com.projet3.atelierdeproduction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// F15 - une ligne figée d'une version de nomenclature : composant et quantité tels qu'ils étaient
@Entity
@Table(name = "ligne_version")
@Getter
@Setter
@NoArgsConstructor
public class LigneVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "version_id", nullable = false)
    private VersionNomenclatureEntity version;

    @ManyToOne
    @JoinColumn(name = "composant_id", nullable = false)
    private ArticleEntity composant;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantite;
}
