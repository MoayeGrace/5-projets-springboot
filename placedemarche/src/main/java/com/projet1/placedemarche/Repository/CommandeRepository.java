package com.projet1.placedemarche.Repository;

import com.projet1.placedemarche.Entity.CommandeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandeRepository extends JpaRepository<CommandeEntity, Long> {

    // F8 : les commandes d'un client, de la plus récente à la plus ancienne
    List<CommandeEntity> findByClientIdOrderByDateCommandeDesc(Long clientId);
}
