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

    @Column(nullable = false, length = 300)
    private String adresse;

    // F12 - montant total que le client peut emprunter (plafond attaché à son profil)
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal plafondEmprunt;

    // F16 - agent de crédit qui suit le client (une alerte d'impayés par agent)
    @Column(nullable = false, length = 150)
    private String agentCredit;

    @OneToMany(mappedBy = "client")
    private List<CompteEntity> comptes = new ArrayList<>();

    @OneToMany(mappedBy = "client")
    private List<PretEntity> prets = new ArrayList<>();
}
