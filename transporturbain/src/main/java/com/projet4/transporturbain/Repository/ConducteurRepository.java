package com.projet4.transporturbain.Repository;

import com.projet4.transporturbain.Entity.ConducteurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConducteurRepository extends JpaRepository<ConducteurEntity, Long> {
}