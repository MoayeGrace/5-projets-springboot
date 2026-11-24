package com.projet2.microfinance.Repository;

import com.projet2.microfinance.Entity.PretEntity;
import com.projet2.microfinance.Entity.StatutPret;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PretRepository extends JpaRepository<PretEntity, Long> {

    // F7 : les prêts d'un client, du plus récent au plus ancien (date d'accord)
    List<PretEntity> findByClientIdOrderByDateAccordDescIdDesc(Long clientId);

    // F10 : les prêts d'un statut donné, renvoyés page par page
    Page<PretEntity> findByStatut(StatutPret statut, Pageable pageable);
}
