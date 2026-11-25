package com.projet5.lapaie.Repository;

import com.projet5.lapaie.Entity.AbsenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbsenceRepository extends JpaRepository<AbsenceEntity, Long> {
}