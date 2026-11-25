package com.projet5.lapaie.Repository;

import com.projet5.lapaie.Entity.BulletinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BulletinRepository extends JpaRepository<BulletinEntity, Long> {

    // F7 : les bulletins d'un employé dont le mois commence par un préfixe (ex : "2026-" pour l'année), mois décroissants
    List<BulletinEntity> findByEmployeIdAndMoisStartingWithOrderByMoisDesc(Long employeId, String prefixeMois);

    // F13 : vrai si l'employé a déjà un bulletin pour ce mois
    boolean existsByEmployeIdAndMois(Long employeId, String mois);

    // F17 : tous les bulletins d'un mois
    List<BulletinEntity> findByMois(String mois);

    // F16 : le bulletin d'un employé pour un mois
    Optional<BulletinEntity> findByEmployeIdAndMois(Long employeId, String mois);

    // F15 : le premier bulletin non édité de l'employé après un mois donné (les mois AAAA-MM se comparent comme du texte)
    Optional<BulletinEntity> findFirstByEmployeIdAndMoisGreaterThanAndEditeFalseOrderByMoisAsc(Long employeId, String mois);
}