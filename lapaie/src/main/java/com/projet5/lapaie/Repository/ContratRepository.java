package com.projet5.lapaie.Repository;

import com.projet5.lapaie.Entity.ContratEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContratRepository extends JpaRepository<ContratEntity, Long> {
}