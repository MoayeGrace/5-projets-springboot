package com.projet2.microfinance.Repository;

import com.projet2.microfinance.Entity.PretEntity;
import com.projet2.microfinance.Entity.StatutPret;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PretRepository extends JpaRepository<PretEntity, Long> {

    // F7 : les prêts d'un client, du plus récent au plus ancien (date d'accord)
    List<PretEntity> findByClientIdOrderByDateAccordDescIdDesc(Long clientId);

    // F10 : les prêts d'un statut donné, renvoyés page par page
    Page<PretEntity> findByStatut(StatutPret statut, Pageable pageable);

    // F12 : nombre de prêts actifs (non soldés) d'un client
    long countByClientIdAndStatutNot(Long clientId, StatutPret statut);

    // F12 : total emprunté sur les prêts actifs (non soldés) d'un client, 0 s'il n'en a aucun
    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM PretEntity p WHERE p.client.id = :clientId AND p.statut <> :statut")
    BigDecimal sommeMontantsSaufStatut(@Param("clientId") Long clientId, @Param("statut") StatutPret statut);
}
