package com.projet2.microfinance.Repository;

import com.projet2.microfinance.Entity.PaiementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<PaiementEntity, Long> {

    // F9 : les paiements d'un prêt, du plus récent au plus ancien
    List<PaiementEntity> findByPretIdOrderByDatePaiementDescIdDesc(Long pretId);
}
