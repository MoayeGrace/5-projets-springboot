package com.projet2.microfinance.Repository;

import com.projet2.microfinance.Entity.PaiementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaiementRepository extends JpaRepository<PaiementEntity, Long> {
}
