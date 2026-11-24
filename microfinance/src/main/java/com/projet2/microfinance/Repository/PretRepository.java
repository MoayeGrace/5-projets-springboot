package com.projet2.microfinance.Repository;

import com.projet2.microfinance.Entity.PretEntity;
import com.projet2.microfinance.Entity.StatutPret;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PretRepository extends JpaRepository<PretEntity, Long> {

    // F7 : les prêts d'un client, du plus récent au plus ancien (date d'accord)
    List<PretEntity> findByClientIdOrderByDateAccordDescIdDesc(Long clientId);

    // F10 : les prêts d'un statut donné, renvoyés page par page
    Page<PretEntity> findByStatut(StatutPret statut, Pageable pageable);

    // F13 - lit le prêt en posant un verrou sur sa ligne (SELECT ... FOR UPDATE) :
    // deux paiements simultanés sur le même prêt sont imputés l'un après l'autre
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PretEntity p WHERE p.id = :id")
    Optional<PretEntity> findByIdAvecVerrou(@Param("id") Long id);

    // F12 : nombre de prêts actifs (non soldés) d'un client
    long countByClientIdAndStatutNot(Long clientId, StatutPret statut);

    // F12 : total emprunté sur les prêts actifs (non soldés) d'un client, 0 s'il n'en a aucun
    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM PretEntity p WHERE p.client.id = :clientId AND p.statut <> :statut")
    BigDecimal sommeMontantsSaufStatut(@Param("clientId") Long clientId, @Param("statut") StatutPret statut);
}
