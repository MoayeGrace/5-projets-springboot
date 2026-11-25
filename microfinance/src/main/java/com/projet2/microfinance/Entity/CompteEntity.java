package com.projet2.microfinance.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "compte")
@Getter
@Setter
@NoArgsConstructor
public class CompteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeCompte type;

    @Column(nullable = false)
    private LocalDate dateOuverture;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal solde = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;
}
