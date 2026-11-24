package com.projet2.microfinance.Repository;

import com.projet2.microfinance.Entity.CompteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompteRepository extends JpaRepository<CompteEntity, Long> {
}
