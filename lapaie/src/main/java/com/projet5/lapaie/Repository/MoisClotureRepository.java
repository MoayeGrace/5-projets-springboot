package com.projet5.lapaie.Repository;

import com.projet5.lapaie.Entity.MoisClotureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// F16 - mois de paie clôturés
@Repository
public interface MoisClotureRepository extends JpaRepository<MoisClotureEntity, Long> {

    // F16 : vrai si le mois (AAAA-MM) est clôturé
    boolean existsByMois(String mois);
}
