package com.projet4.transporturbain.Repository;

import com.projet4.transporturbain.Entity.BusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusRepository extends JpaRepository<BusEntity, Long> {
}