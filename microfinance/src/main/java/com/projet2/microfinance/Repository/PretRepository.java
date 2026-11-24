package com.projet2.microfinance.Repository;

import com.projet2.microfinance.Entity.PretEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PretRepository extends JpaRepository<PretEntity, Long> {
}
