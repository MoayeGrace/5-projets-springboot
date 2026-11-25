package com.projet5.lapaie.Service;

import com.projet5.lapaie.DTO.AbsenceRequestDTO;
import com.projet5.lapaie.DTO.AbsenceResponseDTO;
import com.projet5.lapaie.Entity.AbsenceEntity;
import com.projet5.lapaie.Entity.EmployeEntity;
import com.projet5.lapaie.Repository.AbsenceRepository;
import com.projet5.lapaie.Repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet5.lapaie.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final EmployeRepository employeRepository;

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

    // POST /absences : saisit une nouvelle absence (INSERT)
    public AbsenceResponseDTO creer(AbsenceRequestDTO dto) {
        AbsenceEntity absence = new AbsenceEntity();
        dtoVersEntity(dto, absence);
        return entityVersResponseDTO(absenceRepository.save(absence));
    }

    // PUT /absences/{id} : charge l'élément existant (404 sinon), remplace ses champs, puis UPDATE
    public AbsenceResponseDTO modifier(Long id, AbsenceRequestDTO dto) {
        AbsenceEntity absence = trouverOu404(absenceRepository, id, "Absence");
        dtoVersEntity(dto, absence);
        return entityVersResponseDTO(absenceRepository.save(absence));
    }

    // DELETE /absences/{id} : vérifie que l'élément existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        absenceRepository.delete(trouverOu404(absenceRepository, id, "Absence"));
    }

    // DTO -> Entity (employeId est transformé en objet, 404 s'il n'existe pas)
    private void dtoVersEntity(AbsenceRequestDTO dto, AbsenceEntity absence) {
        EmployeEntity employe = trouverOu404(employeRepository, dto.getEmployeId(), "Employé");

        absence.setType(dto.getType());
        absence.setDateDebut(dto.getDateDebut());
        absence.setDateFin(dto.getDateFin());
        absence.setMotif(dto.getMotif());
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
        dto.setEmployeId(absence.getEmploye().getId());
        dto.setEmployeNom(absence.getEmploye().getNom());
        return dto;
    }
}