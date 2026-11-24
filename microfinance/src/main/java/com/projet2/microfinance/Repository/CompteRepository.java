package com.projet2.microfinance.Repository;

import com.projet2.microfinance.Entity.CompteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompteRepository extends JpaRepository<CompteEntity, Long> {

    // F6 : les comptes d'un client, triés par date d'ouverture (SQL généré à partir du nom de la méthode)
    List<CompteEntity> findByClientIdOrderByDateOuvertureAsc(Long clientId);
}
