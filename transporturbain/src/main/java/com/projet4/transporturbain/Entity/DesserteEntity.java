package com.projet4.transporturbain.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// F6 / F11 - passage d'une ligne à un arrêt : dans un sens donné, à une position donnée (1 = premier arrêt)
// un arrêt ne figure qu'une fois par ligne et par sens
@Entity
@Table(name = "desserte",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ligne_id", "arret_id", "sens"}))
@Getter
@Setter
@NoArgsConstructor
public class DesserteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ligne_id", nullable = false)
    private LigneEntity ligne;

    @ManyToOne
    @JoinColumn(name = "arret_id", nullable = false)
    private ArretEntity arret;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SensLigne sens;

    // Rang de l'arrêt dans l'ordre de desserte
    @Column(nullable = false)
    private Integer position;
}
