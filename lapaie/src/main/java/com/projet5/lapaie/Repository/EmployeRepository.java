package com.projet5.lapaie.Repository;

import com.projet5.lapaie.Entity.EmployeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeRepository extends JpaRepository<EmployeEntity, Long> {
}