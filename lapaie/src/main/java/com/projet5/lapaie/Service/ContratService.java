package com.projet5.lapaie.Service;

import com.projet5.lapaie.DTO.ContratRequestDTO;
import com.projet5.lapaie.DTO.ContratResponseDTO;
import com.projet5.lapaie.Entity.ContratEntity;
import com.projet5.lapaie.Entity.EmployeEntity;
import com.projet5.lapaie.Repository.ContratRepository;
import com.projet5.lapaie.Repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.projet5.lapaie.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class ContratService {

    private final ContratRepository contratRepository;
    private final EmployeRepository employeRepository;

    // GET /contrats : récupère tous les contrats
    public List<ContratResponseDTO> lister() {
        return contratRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /contrats/{id} : récupère un contrat par son id (404 si introuvable)
    public ContratResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(contratRepository, id, "Contrat"));
    }

    // F6 - GET /employes/{id}/contrats : carrière de l'employé, du contrat le plus récent au plus ancien (404 si inconnu)
    public List<ContratResponseDTO> listerParEmploye(Long employeId) {
        trouverOu404(employeRepository, employeId, "Employé");
        return contratRepository.findByEmployeIdOrderByDateDebutDesc(employeId)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // POST /contrats : enregistre un nouveau contrat (INSERT)
    public ContratResponseDTO creer(ContratRequestDTO dto) {
        ContratEntity contrat = new ContratEntity();
        dtoVersEntity(dto, contrat);
        return entityVersResponseDTO(contratRepository.save(contrat));
    }

    // PUT /contrats/{id} : charge l'élément existant (404 sinon), remplace ses champs, puis UPDATE
    public ContratResponseDTO modifier(Long id, ContratRequestDTO dto) {
        ContratEntity contrat = trouverOu404(contratRepository, id, "Contrat");
        dtoVersEntity(dto, contrat);
        return entityVersResponseDTO(contratRepository.save(contrat));
    }

    // DELETE /contrats/{id} : vérifie que l'élément existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        contratRepository.delete(trouverOu404(contratRepository, id, "Contrat"));
    }

    // DTO -> Entity (employeId est transformé en objet, 404 s'il n'existe pas)
    private void dtoVersEntity(ContratRequestDTO dto, ContratEntity contrat) {
        EmployeEntity employe = trouverOu404(employeRepository, dto.getEmployeId(), "Employé");

        contrat.setType(dto.getType());
        contrat.setDateDebut(dto.getDateDebut());
        contrat.setDateFin(dto.getDateFin());
        contrat.setPoste(dto.getPoste());
        contrat.setSalaireBase(dto.getSalaireBase());
        contrat.setEmploye(employe);
    }

    // Entity -> DTO
    private ContratResponseDTO entityVersResponseDTO(ContratEntity contrat) {
        ContratResponseDTO dto = new ContratResponseDTO();
        dto.setId(contrat.getId());
        dto.setType(contrat.getType());
        dto.setDateDebut(contrat.getDateDebut());
        dto.setDateFin(contrat.getDateFin());
        dto.setPoste(contrat.getPoste());
        dto.setSalaireBase(contrat.getSalaireBase());
        dto.setActif(contrat.estActifLe(LocalDate.now()));   // F6
        dto.setEmployeId(contrat.getEmploye().getId());
        dto.setEmployeNom(contrat.getEmploye().getNom());
        return dto;
    }
}