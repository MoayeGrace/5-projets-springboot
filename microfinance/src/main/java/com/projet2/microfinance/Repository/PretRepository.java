package com.projet2.microfinance.Repository;

import com.projet2.microfinance.Entity.PretEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PretRepository extends JpaRepository<PretEntity, Long> {

    // F7 : les prêts d'un client, du plus récent au plus ancien (date d'accord)
    List<PretEntity> findByClientIdOrderByDateAccordDescIdDesc(Long clientId);
}
