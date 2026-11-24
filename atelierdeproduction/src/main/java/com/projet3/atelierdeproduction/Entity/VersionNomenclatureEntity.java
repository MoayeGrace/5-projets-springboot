package com.projet3.atelierdeproduction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// F15 - version datée de la nomenclature d'un article : une copie figée de ses lignes au moment de l'application
@Entity
@Table(name = "version_nomenclature",
        uniqueConstraints = @UniqueConstraint(columnNames = {"article_id", "numero"}))
@Getter
@Setter
@NoArgsConstructor
public class VersionNomenclatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleEntity article;

    // Numéro de version : 1, 2, 3...
    @Column(nullable = false)
    private Integer numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutVersion statut = StatutVersion.EN_PREPARATION;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    // Date à partir de laquelle la version s'applique (null tant qu'elle est en préparation)
    private LocalDateTime dateApplication;

    // Lignes figées ; cascade ALL : enregistrées avec la version
    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneVersionEntity> lignes = new ArrayList<>();
}
