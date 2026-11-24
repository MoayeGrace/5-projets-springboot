package com.projet4.transporturbain.Repository;

import com.projet4.transporturbain.Entity.LigneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LigneRepository extends JpaRepository<LigneEntity, Long> {
}