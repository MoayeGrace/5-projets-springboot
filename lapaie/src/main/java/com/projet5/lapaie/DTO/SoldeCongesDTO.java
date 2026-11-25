package com.projet5.lapaie.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

// F18 - droits à congés d'un employé à une date : acquis, consommés et solde
@Getter
@Setter
@NoArgsConstructor
public class SoldeCongesDTO {

    private Long employeId;
    private String employeNom;
    private LocalDate date;
    private long moisTravailles;        // mois complets depuis l'embauche
    private BigDecimal joursAcquis;     // 2,5 jours par mois travaillé
    private long joursConsommes;        // jours d'absence de type CONGE_PAYE jusqu'à la date
    private BigDecimal solde;           // acquis - consommés (peut être négatif)
    private boolean soldeNegatif;       // signale un solde négatif
}
