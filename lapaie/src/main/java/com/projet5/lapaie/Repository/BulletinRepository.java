package com.projet5.lapaie.Repository;

import com.projet5.lapaie.Entity.BulletinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BulletinRepository extends JpaRepository<BulletinEntity, Long> {

    // F7 : les bulletins d'un employé dont le mois commence par un préfixe (ex : "2026-" pour l'année), mois décroissants
    List<BulletinEntity> findByEmployeIdAndMoisStartingWithOrderByMoisDesc(Long employeId, String prefixeMois);

    // F13 : vrai si l'employé a déjà un bulletin pour ce mois
    boolean existsByEmployeIdAndMois(Long employeId, String mois);
}