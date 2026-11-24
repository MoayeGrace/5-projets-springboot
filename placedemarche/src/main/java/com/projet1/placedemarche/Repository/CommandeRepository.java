package com.projet1.placedemarche.Repository;

import com.projet1.placedemarche.Entity.CommandeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommandeRepository extends JpaRepository<CommandeEntity, Long> {
}