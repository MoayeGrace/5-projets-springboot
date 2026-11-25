package com.projet5.lapaie.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "rubrique")
@Getter
@Setter
@NoArgsConstructor
public class RubriqueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Code court de la rubrique (ex : SAL_BASE, COT_RET)
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeRubrique type;

    // Taux en pourcentage (ex : 6.3000 pour 6,3 %)
    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal taux;

    // F13 - rang de la rubrique dans le calcul du bulletin (1 = appliquée en premier)
    @Column(nullable = false, unique = true)
    private Integer ordreCalcul;
}
