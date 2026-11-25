package com.projet5.lapaie.Repository;

import com.projet5.lapaie.Entity.RubriqueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RubriqueRepository extends JpaRepository<RubriqueEntity, Long> {
}