package com.projet2.microfinance.Repository;

import com.projet2.microfinance.Entity.EcheanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EcheanceRepository extends JpaRepository<EcheanceEntity, Long> {

    // F8 : les échéances d'un prêt, triées par date croissante
    List<EcheanceEntity> findByPretIdOrderByDateEcheanceAsc(Long pretId);

    // F16 / F18 : échéances impayées à une date (date passée, reste à payer positif), hors prêts soldés
    @Query("""
            SELECT e FROM EcheanceEntity e
            WHERE e.dateEcheance < :date
              AND e.pret.statut <> com.projet2.microfinance.Entity.StatutPret.SOLDE
              AND (e.montantTotal + e.penalites - e.capitalPaye - e.interetsPaye - e.penalitesPaye) > 0
            ORDER BY e.pret.client.nom, e.dateEcheance
            """)
    List<EcheanceEntity> findImpayees(@Param("date") LocalDate date);
}
