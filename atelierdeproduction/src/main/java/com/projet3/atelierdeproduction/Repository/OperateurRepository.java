package com.projet3.atelierdeproduction.Repository;

import com.projet3.atelierdeproduction.Entity.OperateurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperateurRepository extends JpaRepository<OperateurEntity, Long> {
}