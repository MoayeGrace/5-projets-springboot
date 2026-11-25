package com.projet2.microfinance.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pret")
@Getter
@Setter
@NoArgsConstructor
public class PretEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private Integer dureeMois;

    // Taux d'intérêt annuel en pourcentage (ex : 12.00 pour 12 %)
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taux;

    @Column(nullable = false)
    private LocalDate dateAccord;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutPret statut = StatutPret.ACCORDE;

    // F15 - date à laquelle le prêt a été entièrement remboursé
    private LocalDate dateSolde;

    // F16 - plus grand nombre de jours de retard constaté sur ce prêt
    @Column(nullable = false)
    private Integer joursRetardMax = 0;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @OneToMany(mappedBy = "pret")
    @OrderBy("dateEcheance ASC")
    private List<EcheanceEntity> echeances = new ArrayList<>();

    @OneToMany(mappedBy = "pret")
    private List<PaiementEntity> paiements = new ArrayList<>();

    // F15 - vrai quand capital, intérêts et pénalités de toutes les échéances sont remboursés
    public boolean estEntierementRembourse() {
        return !echeances.isEmpty() && echeances.stream().allMatch(e -> e.resteAPayer().signum() == 0);
    }

    // F7 - capital restant dû : 0 si soldé, le montant entier si l'échéancier n'existe pas encore, sinon la somme du capital non remboursé
    public BigDecimal capitalRestantDu() {
        if (statut == StatutPret.SOLDE) {
            return BigDecimal.ZERO;
        }
        if (echeances.isEmpty()) {
            return montant;
        }
        return echeances.stream()
                .map(EcheanceEntity::capitalDu)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
