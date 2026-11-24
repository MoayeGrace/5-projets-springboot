package com.projet2.microfinance.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, length = 30)
    private String telephone;

    @Column(nullable = false, length = 255)
    private String adresse;

    // Montant total que le client peut emprunter (plafond attaché à son profil)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal plafondEmprunt;

    @OneToMany(mappedBy = "client")
    private List<CompteEntity> comptes = new ArrayList<>();

    @OneToMany(mappedBy = "client")
    private List<PretEntity> prets = new ArrayList<>();
}
