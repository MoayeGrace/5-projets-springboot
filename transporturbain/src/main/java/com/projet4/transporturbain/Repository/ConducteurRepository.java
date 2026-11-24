package com.projet4.transporturbain.Repository;

import com.projet4.transporturbain.Entity.ConducteurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConducteurRepository extends JpaRepository<ConducteurEntity, Long> {

    // F16 : conducteurs dont le permis expire entre deux dates et qui n'ont pas encore été signalés
    List<ConducteurEntity> findByDateValiditePermisBetweenAndAlertePermisEnvoyeeFalseOrderByDateValiditePermisAsc(LocalDate du, LocalDate au);
}