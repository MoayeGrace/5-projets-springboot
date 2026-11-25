package com.projet5.lapaie.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// F16 - mois de paie arrêté définitivement : plus aucune absence ni aucun bulletin ne peut y être créé ou modifié
@Entity
@Table(name = "mois_cloture")
@Getter
@Setter
@NoArgsConstructor
public class MoisClotureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mois clôturé au format AAAA-MM
    @Column(nullable = false, unique = true, length = 7)
    private String mois;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCloture;
}
