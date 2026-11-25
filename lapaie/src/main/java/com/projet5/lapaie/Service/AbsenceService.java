package com.projet5.lapaie.Service;

import com.projet5.lapaie.DTO.AbsenceRequestDTO;
import com.projet5.lapaie.DTO.AbsenceResponseDTO;
import com.projet5.lapaie.Entity.AbsenceEntity;
import com.projet5.lapaie.Entity.EmployeEntity;
import com.projet5.lapaie.Entity.TypeAbsence;
import com.projet5.lapaie.Repository.AbsenceRepository;
import com.projet5.lapaie.Repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static com.projet5.lapaie.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final EmployeRepository employeRepository;
    private final ClotureService clotureService;   // F16

    // GET /absences : récupère toutes les absences
    public List<AbsenceResponseDTO> lister() {
        return absenceRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /absences/{id} : récupère une absence par son id (404 si introuvable)
    public AbsenceResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(absenceRepository, id, "Absence"));
    }

    // F8 - GET /employes/{id}/absences?du=&au= : absences touchant la période (mois en cours par défaut), renvoyées
    // entières même si elles débordent ; de la plus récente à la plus ancienne (400 si "du" est après "au")
    public List<AbsenceResponseDTO> listerParEmploye(Long employeId, LocalDate du, LocalDate au) {
        trouverOu404(employeRepository, employeId, "Employé");
        YearMonth moisCourant = YearMonth.now();
        LocalDate debut = (du != null) ? du : moisCourant.atDay(1);
        LocalDate fin = (au != null) ? au : moisCourant.atEndOfMonth();
        if (debut.isAfter(fin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de début doit précéder la date de fin");
        }
        return absenceRepository
                .findByEmployeIdAndDateDebutLessThanEqualAndDateFinGreaterThanEqualOrderByDateDebutDesc(employeId, fin, debut)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // POST /absences : saisit une nouvelle absence (INSERT)
    public AbsenceResponseDTO creer(AbsenceRequestDTO dto) {
        verifierPeriodeLibre(dto, null);   // F12
        AbsenceEntity absence = new AbsenceEntity();
        dtoVersEntity(dto, absence);
        return entityVersResponseDTO(absenceRepository.save(absence));
    }

    // F12 - aucune autre absence de l'employé ne doit recouvrir la période (409 citant l'absence, son type et ses dates) ;
    // deux absences contiguës sont acceptées
    private void verifierPeriodeLibre(AbsenceRequestDTO dto, Long idExclu) {
        if (dto.getDateFin().isBefore(dto.getDateDebut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de fin précède la date de début");
        }
        clotureService.verifierPeriodeOuverte(dto.getDateDebut(), dto.getDateFin());   // F16 - pas d'absence sur un mois clôturé
        for (AbsenceEntity autre : absenceRepository.findByEmployeId(dto.getEmployeId())) {
            if (!autre.getId().equals(idExclu) && autre.chevauche(dto.getDateDebut(), dto.getDateFin())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Période en conflit avec l'absence " + autre.getId() + " (" + autre.getType() + " du "
                                + autre.getDateDebut() + " au " + autre.getDateFin() + ")");
            }
        }
    }

    // PUT /absences/{id} : charge l'élément existant (404 sinon), remplace ses champs, puis UPDATE
    public AbsenceResponseDTO modifier(Long id, AbsenceRequestDTO dto) {
        AbsenceEntity absence = trouverOu404(absenceRepository, id, "Absence");
        clotureService.verifierPeriodeOuverte(absence.getDateDebut(), absence.getDateFin());   // F16 - ancienne période
        verifierPeriodeLibre(dto, id);     // F12 - une date corrigée ne doit pas créer de chevauchement
        dtoVersEntity(dto, absence);
        return entityVersResponseDTO(absenceRepository.save(absence));
    }

    // DELETE /absences/{id} : vérifie que l'élément existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        AbsenceEntity absence = trouverOu404(absenceRepository, id, "Absence");
        clotureService.verifierPeriodeOuverte(absence.getDateDebut(), absence.getDateFin());   // F16
        absenceRepository.delete(absence);
    }

    // DTO -> Entity (employeId est transformé en objet, 404 s'il n'existe pas)
    private void dtoVersEntity(AbsenceRequestDTO dto, AbsenceEntity absence) {
        EmployeEntity employe = trouverOu404(employeRepository, dto.getEmployeId(), "Employé");

        absence.setType(dto.getType());
        absence.setDateDebut(dto.getDateDebut());
        absence.setDateFin(dto.getDateFin());
        absence.setMotif(dto.getMotif());
        absence.setRemuneree(dto.getRemuneree() != null ? dto.getRemuneree() : dto.getType() != TypeAbsence.SANS_SOLDE);   // F13
        absence.setEmploye(employe);
    }

    // Entity -> DTO
    private AbsenceResponseDTO entityVersResponseDTO(AbsenceEntity absence) {
        AbsenceResponseDTO dto = new AbsenceResponseDTO();
        dto.setId(absence.getId());
        dto.setType(absence.getType());
        dto.setDateDebut(absence.getDateDebut());
        dto.setDateFin(absence.getDateFin());
        dto.setMotif(absence.getMotif());
        dto.setRemuneree(absence.getRemuneree());   // F13
        dto.setDureeJours(absence.dureeJours());   // F8
        dto.setEmployeId(absence.getEmploye().getId());
        dto.setEmployeNom(absence.getEmploye().getNom());
        return dto;
    }
}