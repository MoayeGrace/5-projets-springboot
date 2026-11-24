package com.projet2.microfinance.Repository;

import com.projet2.microfinance.Entity.EcheanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EcheanceRepository extends JpaRepository<EcheanceEntity, Long> {
}
