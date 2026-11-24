package com.projet1.placedemarche.Service;

import com.projet1.placedemarche.DTO.CategorieRequestDTO;
import com.projet1.placedemarche.DTO.CategorieResponseDTO;
import com.projet1.placedemarche.Entity.CategorieEntity;
import com.projet1.placedemarche.Repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet1.placedemarche.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class CategorieService {

    private final CategorieRepository categorieRepository;

    // GET /categories : récupère toutes les catégories et les convertit en DTO
    public List<CategorieResponseDTO> lister() {
        return categorieRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /categories/{id} : récupère une catégorie par son id (404 si elle n'existe pas)
    public CategorieResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(categorieRepository, id, "Catégorie"));
    }

    // POST /categories : crée une nouvelle catégorie (INSERT) et la renvoie avec son id
    public CategorieResponseDTO creer(CategorieRequestDTO dto) {
        CategorieEntity categorie = new CategorieEntity();
        dtoVersEntity(dto, categorie);
        return entityVersResponseDTO(categorieRepository.save(categorie));
    }

    // PUT /categories/{id} : charge la catégorie existante (404 sinon), remplace ses champs, puis UPDATE
    public CategorieResponseDTO modifier(Long id, CategorieRequestDTO dto) {
        CategorieEntity categorie = trouverOu404(categorieRepository, id, "Catégorie");
        dtoVersEntity(dto, categorie);
        return entityVersResponseDTO(categorieRepository.save(categorie));
    }

    // DELETE /categories/{id} : vérifie que la catégorie existe (404 sinon), puis la supprime
    public void supprimer(Long id) {
        categorieRepository.delete(trouverOu404(categorieRepository, id, "Catégorie"));
    }

    // DTO -> Entity
    private void dtoVersEntity(CategorieRequestDTO dto, CategorieEntity categorie) {
        categorie.setLibelle(dto.getLibelle());
        categorie.setDescription(dto.getDescription());
    }

    // Entity -> DTO
    private CategorieResponseDTO entityVersResponseDTO(CategorieEntity categorie) {
        CategorieResponseDTO dto = new CategorieResponseDTO();
        dto.setId(categorie.getId());
        dto.setLibelle(categorie.getLibelle());
        dto.setDescription(categorie.getDescription());
        return dto;
    }
}
