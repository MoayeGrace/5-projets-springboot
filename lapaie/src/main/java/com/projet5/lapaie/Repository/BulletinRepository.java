package com.projet5.lapaie.Repository;

import com.projet5.lapaie.Entity.BulletinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulletinRepository extends JpaRepository<BulletinEntity, Long> {
}