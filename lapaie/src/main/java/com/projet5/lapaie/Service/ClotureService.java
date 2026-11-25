package com.projet5.lapaie.Service;

import com.projet5.lapaie.DTO.ClotureResponseDTO;
import com.projet5.lapaie.Entity.BulletinEntity;
import com.projet5.lapaie.Entity.EmployeEntity;
import com.projet5.lapaie.Entity.EtatBulletin;
import com.projet5.lapaie.Repository.BulletinRepository;
import com.projet5.lapaie.Repository.ContratRepository;
import com.projet5.lapaie.Repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClotureService {

    private final EmployeRepository employeRepository;
    private final ContratRepository contratRepository;
    private final BulletinRepository bulletinRepository;

    // F16 - POST /mois/{mois}/cloture : arrête le mois si chaque employé ayant un contrat actif ce mois a un bulletin
    // édité ; ses bulletins passent alors à l'état CLOTURE. 409 listant les employés sans bulletin édité, 409 si déjà
    // clôturé, 400 si le format est faux
    @Transactional
    public ClotureResponseDTO cloturer(String mois) {
        YearMonth periode = lireMois(mois);
        verifierMoisOuvert(mois);

        List<String> sansBulletinEdite = new ArrayList<>();
        List<BulletinEntity> edites = new ArrayList<>();
        for (EmployeEntity employe : employeRepository.findAll()) {
            boolean contratActif = contratRepository.findByEmployeId(employe.getId()).stream()
                    .anyMatch(c -> c.chevauche(periode.atDay(1), periode.atEndOfMonth()));
            if (!contratActif) {
                continue;
            }
            Optional<BulletinEntity> bulletin = bulletinRepository.findByEmployeIdAndMois(employe.getId(), mois);
            if (bulletin.isPresent() && bulletin.get().getEtat() == EtatBulletin.EDITE) {
                edites.add(bulletin.get());
            } else {
                sansBulletinEdite.add(employe.getMatricule() + " " + employe.getNom());
            }
        }
        if (!sansBulletinEdite.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Clôture impossible, employés sans bulletin édité en " + mois + " : " + String.join(", ", sansBulletinEdite));
        }
        edites.forEach(bulletin -> bulletin.setEtat(EtatBulletin.CLOTURE));

        ClotureResponseDTO dto = new ClotureResponseDTO();
        dto.setMois(mois);
        dto.setDateCloture(LocalDateTime.now());
        dto.setNombreBulletins(edites.size());
        return dto;
    }

    // F16 - 409 si le mois (AAAA-MM) est clôturé (ses bulletins sont à l'état CLOTURE) : plus de création ni de modification
    public void verifierMoisOuvert(String mois) {
        if (bulletinRepository.existsByMoisAndEtat(mois, EtatBulletin.CLOTURE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le mois " + mois + " est clôturé : aucune modification possible");
        }
    }

    // F16 - 409 si l'un des mois touchés par la période [debut, fin] est clôturé (pour les absences)
    public void verifierPeriodeOuverte(LocalDate debut, LocalDate fin) {
        for (YearMonth m = YearMonth.from(debut); !m.isAfter(YearMonth.from(fin)); m = m.plusMonths(1)) {
            verifierMoisOuvert(m.toString());
        }
    }

    // F16 - convertit "AAAA-MM" en YearMonth (400 si le format est invalide)
    private YearMonth lireMois(String mois) {
        if (!mois.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le mois doit être au format AAAA-MM");
        }
        return YearMonth.parse(mois);
    }
}
