package com.projet5.lapaie.Service;

import com.projet5.lapaie.DTO.RubriqueRequestDTO;
import com.projet5.lapaie.DTO.RubriqueResponseDTO;
import com.projet5.lapaie.Entity.RubriqueEntity;
import com.projet5.lapaie.Repository.RubriqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet5.lapaie.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class RubriqueService {

    private final RubriqueRepository rubriqueRepository;

    // GET /rubriques : récupère tout le référentiel des rubriques
    public List<RubriqueResponseDTO> lister() {
        return rubriqueRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /rubriques/{id} : récupère une rubrique par son id (404 si introuvable)
    public RubriqueResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(rubriqueRepository, id, "Rubrique"));
    }

    // POST /rubriques : crée une nouvelle rubrique (INSERT)
    public RubriqueResponseDTO creer(RubriqueRequestDTO dto) {
        RubriqueEntity rubrique = new RubriqueEntity();
        dtoVersEntity(dto, rubrique);
        return entityVersResponseDTO(rubriqueRepository.save(rubrique));
    }

    // PUT /rubriques/{id} : charge l'élément existant (404 sinon), remplace ses champs, puis UPDATE
    public RubriqueResponseDTO modifier(Long id, RubriqueRequestDTO dto) {
        RubriqueEntity rubrique = trouverOu404(rubriqueRepository, id, "Rubrique");
        dtoVersEntity(dto, rubrique);
        return entityVersResponseDTO(rubriqueRepository.save(rubrique));
    }

    // DELETE /rubriques/{id} : vérifie que l'élément existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        rubriqueRepository.delete(trouverOu404(rubriqueRepository, id, "Rubrique"));
    }

    // DTO -> Entity
    private void dtoVersEntity(RubriqueRequestDTO dto, RubriqueEntity rubrique) {
        rubrique.setCode(dto.getCode());
        rubrique.setLibelle(dto.getLibelle());
        rubrique.setType(dto.getType());
        rubrique.setTaux(dto.getTaux());
        rubrique.setOrdreCalcul(dto.getOrdreCalcul());   // F13
    }

    // Entity -> DTO
    private RubriqueResponseDTO entityVersResponseDTO(RubriqueEntity rubrique) {
        RubriqueResponseDTO dto = new RubriqueResponseDTO();
        dto.setId(rubrique.getId());
        dto.setCode(rubrique.getCode());
        dto.setLibelle(rubrique.getLibelle());
        dto.setType(rubrique.getType());
        dto.setTaux(rubrique.getTaux());
        dto.setOrdreCalcul(rubrique.getOrdreCalcul());   // F13
        return dto;
    }
}