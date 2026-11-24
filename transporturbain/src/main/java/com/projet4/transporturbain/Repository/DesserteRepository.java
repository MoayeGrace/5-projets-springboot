package com.projet4.transporturbain.Repository;

import com.projet4.transporturbain.Entity.DesserteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// F6 / F11 - ordre de desserte des lignes
@Repository
public interface DesserteRepository extends JpaRepository<DesserteEntity, Long> {

    // F6 : les arrêts d'une ligne, sens par sens, dans l'ordre de desserte
    List<DesserteEntity> findByLigneIdOrderBySensAscPositionAsc(Long ligneId);

    // F9 : les passages de lignes à un arrêt
    List<DesserteEntity> findByArretIdOrderByLigneNumeroAsc(Long arretId);
}
