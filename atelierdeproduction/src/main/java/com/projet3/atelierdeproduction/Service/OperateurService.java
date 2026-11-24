package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.DTO.OperateurRequestDTO;
import com.projet3.atelierdeproduction.DTO.OperateurResponseDTO;
import com.projet3.atelierdeproduction.Entity.OperateurEntity;
import com.projet3.atelierdeproduction.Repository.OperateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet3.atelierdeproduction.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class OperateurService {

    private final OperateurRepository operateurRepository;

    // GET /operateurs : récupère tout l'effectif
    public List<OperateurResponseDTO> lister() {
        return operateurRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /operateurs/{id} : récupère la fiche d'un opérateur (404 s'il n'existe pas)
    public OperateurResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(operateurRepository, id, "Opérateur"));
    }

    // POST /operateurs : crée une nouvelle fiche opérateur (INSERT)
    public OperateurResponseDTO creer(OperateurRequestDTO dto) {
        OperateurEntity operateur = new OperateurEntity();
        dtoVersEntity(dto, operateur);
        return entityVersResponseDTO(operateurRepository.save(operateur));
    }

    // PUT /operateurs/{id} : charge l'opérateur existant (404 sinon), remplace ses champs, puis UPDATE
    public OperateurResponseDTO modifier(Long id, OperateurRequestDTO dto) {
        OperateurEntity operateur = trouverOu404(operateurRepository, id, "Opérateur");
        dtoVersEntity(dto, operateur);
        return entityVersResponseDTO(operateurRepository.save(operateur));
    }

    // DELETE /operateurs/{id} : vérifie que l'opérateur existe (404 sinon), puis supprime sa fiche
    public void supprimer(Long id) {
        operateurRepository.delete(trouverOu404(operateurRepository, id, "Opérateur"));
    }

    // DTO -> Entity
    private void dtoVersEntity(OperateurRequestDTO dto, OperateurEntity operateur) {
        operateur.setNom(dto.getNom());
        operateur.setMatricule(dto.getMatricule());
        operateur.setEquipe(dto.getEquipe());
    }

    // Entity -> DTO
    private OperateurResponseDTO entityVersResponseDTO(OperateurEntity operateur) {
        OperateurResponseDTO dto = new OperateurResponseDTO();
        dto.setId(operateur.getId());
        dto.setNom(operateur.getNom());
        dto.setMatricule(operateur.getMatricule());
        dto.setEquipe(operateur.getEquipe());
        return dto;
    }
}
