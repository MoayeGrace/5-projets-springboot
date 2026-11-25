package com.projet3.atelierdeproduction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "machine")
@Getter
@Setter
@NoArgsConstructor
public class MachineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 50)
    private String atelier;

    // F19 - heures ouvrables par jour (du lundi au vendredi, à partir de 8h)
    @Column(nullable = false)
    private Integer heuresOuvrablesParJour = 8;

    // F12 / F19 - arrêt de maintenance prévu (null = aucun)
    private LocalDateTime debutMaintenance;

    private LocalDateTime finMaintenance;

    @OneToMany(mappedBy = "machine")
    private List<OrdreFabricationEntity> ordres = new ArrayList<>();
}
