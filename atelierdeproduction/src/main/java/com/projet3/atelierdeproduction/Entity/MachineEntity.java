package com.projet3.atelierdeproduction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false, length = 100)
    private String atelier;

    @OneToMany(mappedBy = "machine")
    private List<OrdreFabricationEntity> ordres = new ArrayList<>();
}
