package com.projet3.atelierdeproduction.Repository;

import com.projet3.atelierdeproduction.Entity.OrdreFabricationEntity;
import com.projet3.atelierdeproduction.Entity.StatutOrdre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdreFabricationRepository extends JpaRepository<OrdreFabricationEntity, Long> {

    // F8 : tous les ordres, triés par date prévue
    List<OrdreFabricationEntity> findAllByOrderByDatePrevueAsc();

    // F8 : les ordres d'un statut donné, triés par date prévue
    List<OrdreFabricationEntity> findByStatutOrderByDatePrevueAsc(StatutOrdre statut);

    // F16 : ordres dont la date prévue est passée et dont le statut fait partie de la liste (ex : PLANIFIE, LANCE)
    List<OrdreFabricationEntity> findByDatePrevueBeforeAndStatutInOrderByDatePrevueAsc(LocalDateTime date, List<StatutOrdre> statuts);

    // F15 : les ordres d'un statut donné modifiés depuis un instant (ex : TERMINE aujourd'hui)
    List<OrdreFabricationEntity> findByStatutAndDateModificationGreaterThanEqual(StatutOrdre statut, LocalDateTime depuis);

    // F12 : les ordres d'une machine qui n'ont pas un statut donné (ex : hors annulés)
    List<OrdreFabricationEntity> findByMachineIdAndStatutNot(Long machineId, StatutOrdre statut);

    // F15 : un ordre utilise-t-il cette version de la nomenclature de l'article ?
    boolean existsByArticleIdAndVersionNomenclature(Long articleId, Integer versionNomenclature);

    // F9 : les ordres d'une machine dont la date prévue est dans [debut, fin[, triés par date prévue
    List<OrdreFabricationEntity> findByMachineIdAndDatePrevueGreaterThanEqualAndDatePrevueLessThanOrderByDatePrevueAsc(
            Long machineId, LocalDateTime debut, LocalDateTime fin);
}