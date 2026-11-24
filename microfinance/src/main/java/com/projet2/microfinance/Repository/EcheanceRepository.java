package com.projet2.microfinance.Repository;

import com.projet2.microfinance.Entity.EcheanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EcheanceRepository extends JpaRepository<EcheanceEntity, Long> {

    // F8 : les échéances d'un prêt, triées par date croissante
    List<EcheanceEntity> findByPretIdOrderByDateEcheanceAscNumeroAsc(Long pretId);
}
