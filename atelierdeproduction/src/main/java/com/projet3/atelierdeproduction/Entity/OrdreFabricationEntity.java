package com.projet3.atelierdeproduction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordre_fabrication")
@Getter
@Setter
@NoArgsConstructor
public class OrdreFabricationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Article à produire
    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleEntity article;

    // Nombre d'unités à produire
    @Column(nullable = false)
    private Integer quantite;

    // Date et heure prévues du début de la production
    @Column(nullable = false)
    private LocalDateTime datePrevue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutOrdre statut = StatutOrdre.PLANIFIE;

    @ManyToOne
    @JoinColumn(name = "machine_id", nullable = false)
    private MachineEntity machine;

    @ManyToOne
    @JoinColumn(name = "operateur_id", nullable = false)
    private OperateurEntity operateur;

    // F13 - historique des changements de statut ; cascade ALL : enregistré et supprimé avec l'ordre
    @OneToMany(mappedBy = "ordre", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateChangement ASC")
    private List<HistoriqueStatutEntity> historique = new ArrayList<>();

    // F12 - fin du créneau machine : début + quantité × temps unitaire de la gamme
    public LocalDateTime dateFinPrevue() {
        return datePrevue.plusMinutes((long) quantite * article.getTempsUnitaireMinutes());
    }

    // F12 - vrai si le créneau de cet ordre recouvre l'intervalle [debut, fin[
    public boolean chevauche(LocalDateTime debut, LocalDateTime fin) {
        return datePrevue.isBefore(fin) && dateFinPrevue().isAfter(debut);
    }
}
