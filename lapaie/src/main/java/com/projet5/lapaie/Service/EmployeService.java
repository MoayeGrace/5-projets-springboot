package com.projet5.lapaie.Service;

import com.projet5.lapaie.DTO.EmployeRequestDTO;
import com.projet5.lapaie.DTO.EmployeResponseDTO;
import com.projet5.lapaie.Entity.EmployeEntity;
import com.projet5.lapaie.Repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet5.lapaie.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class EmployeService {

    private final EmployeRepository employeRepository;

    // GET /employes : récupère tout l'effectif
    public List<EmployeResponseDTO> lister() {
        return employeRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /employes/{id} : récupère un employé par son id (404 si introuvable)
    public EmployeResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(employeRepository, id, "Employé"));
    }

    // POST /employes : crée une nouvelle fiche employé (INSERT)
    public EmployeResponseDTO creer(EmployeRequestDTO dto) {
        EmployeEntity employe = new EmployeEntity();
        dtoVersEntity(dto, employe);
        return entityVersResponseDTO(employeRepository.save(employe));
    }

    // PUT /employes/{id} : charge l'élément existant (404 sinon), remplace ses champs, puis UPDATE
    public EmployeResponseDTO modifier(Long id, EmployeRequestDTO dto) {
        EmployeEntity employe = trouverOu404(employeRepository, id, "Employé");
        dtoVersEntity(dto, employe);
        return entityVersResponseDTO(employeRepository.save(employe));
    }

    // DELETE /employes/{id} : vérifie que l'élément existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        employeRepository.delete(trouverOu404(employeRepository, id, "Employé"));
    }

    // DTO -> Entity
    private void dtoVersEntity(EmployeRequestDTO dto, EmployeEntity employe) {
        employe.setNom(dto.getNom());
        employe.setMatricule(dto.getMatricule());
        employe.setDateEmbauche(dto.getDateEmbauche());
        employe.setService(dto.getService());
    }

    // Entity -> DTO
    private EmployeResponseDTO entityVersResponseDTO(EmployeEntity employe) {
        EmployeResponseDTO dto = new EmployeResponseDTO();
        dto.setId(employe.getId());
        dto.setNom(employe.getNom());
        dto.setMatricule(employe.getMatricule());
        dto.setDateEmbauche(employe.getDateEmbauche());
        dto.setService(employe.getService());
        return dto;
    }
}