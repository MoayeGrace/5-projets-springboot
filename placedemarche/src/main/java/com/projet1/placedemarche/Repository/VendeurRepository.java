package com.projet1.placedemarche.Repository;

import com.projet1.placedemarche.Entity.VendeurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendeurRepository extends JpaRepository<VendeurEntity, Long> {
}