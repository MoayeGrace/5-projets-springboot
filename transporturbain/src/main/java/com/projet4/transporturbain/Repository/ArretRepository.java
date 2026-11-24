package com.projet4.transporturbain.Repository;

import com.projet4.transporturbain.Entity.ArretEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArretRepository extends JpaRepository<ArretEntity, Long> {
}