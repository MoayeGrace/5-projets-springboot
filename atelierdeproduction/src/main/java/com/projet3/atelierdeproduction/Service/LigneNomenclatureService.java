package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.DTO.LigneNomenclatureRequestDTO;
import com.projet3.atelierdeproduction.DTO.LigneNomenclatureResponseDTO;
import com.projet3.atelierdeproduction.Entity.ArticleEntity;
import com.projet3.atelierdeproduction.Entity.LigneNomenclatureEntity;
import com.projet3.atelierdeproduction.Entity.TypeArticle;
import com.projet3.atelierdeproduction.Repository.ArticleRepository;
import com.projet3.atelierdeproduction.Repository.LigneNomenclatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.projet3.atelierdeproduction.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class LigneNomenclatureService {

    private final LigneNomenclatureRepository ligneNomenclatureRepository;
    private final ArticleRepository articleRepository;

    // GET /nomenclatures : récupère toutes les lignes de nomenclature
    public List<LigneNomenclatureResponseDTO> lister() {
        return ligneNomenclatureRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /nomenclatures/{id} : récupère une ligne par son id (404 si elle n'existe pas)
    public LigneNomenclatureResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(ligneNomenclatureRepository, id, "Ligne de nomenclature"));
    }

    // POST /nomenclatures : saisit une nouvelle ligne (INSERT) : composé, composant et quantité
    public LigneNomenclatureResponseDTO creer(LigneNomenclatureRequestDTO dto) {
        LigneNomenclatureEntity ligne = new LigneNomenclatureEntity();
        dtoVersEntity(dto, ligne);
        return entityVersResponseDTO(ligneNomenclatureRepository.save(ligne));
    }

    // PUT /nomenclatures/{id} : charge la ligne existante (404 sinon), ajuste ses champs, puis UPDATE
    public LigneNomenclatureResponseDTO modifier(Long id, LigneNomenclatureRequestDTO dto) {
        LigneNomenclatureEntity ligne = trouverOu404(ligneNomenclatureRepository, id, "Ligne de nomenclature");
        dtoVersEntity(dto, ligne);
        return entityVersResponseDTO(ligneNomenclatureRepository.save(ligne));
    }

    // DELETE /nomenclatures/{id} : vérifie que la ligne existe (404 sinon), puis la supprime
    public void supprimer(Long id) {
        ligneNomenclatureRepository.delete(trouverOu404(ligneNomenclatureRepository, id, "Ligne de nomenclature"));
    }

    // DTO -> Entity (ids transformés en articles, 404 s'ils n'existent pas ; seul un article FABRIQUE a une nomenclature)
    private void dtoVersEntity(LigneNomenclatureRequestDTO dto, LigneNomenclatureEntity ligne) {
        ArticleEntity compose = trouverOu404(articleRepository, dto.getComposeId(), "Article");
        ArticleEntity composant = trouverOu404(articleRepository, dto.getComposantId(), "Article");
        if (compose.getType() != TypeArticle.FABRIQUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'article " + compose.getReference() + " est acheté : il n'a pas de nomenclature");
        }

        ligne.setCompose(compose);
        ligne.setComposant(composant);
        ligne.setQuantite(dto.getQuantite());
    }

    // Entity -> DTO
    private LigneNomenclatureResponseDTO entityVersResponseDTO(LigneNomenclatureEntity ligne) {
        LigneNomenclatureResponseDTO dto = new LigneNomenclatureResponseDTO();
        dto.setId(ligne.getId());
        dto.setComposeId(ligne.getCompose().getId());
        dto.setComposeReference(ligne.getCompose().getReference());
        dto.setComposantId(ligne.getComposant().getId());
        dto.setComposantReference(ligne.getComposant().getReference());
        dto.setComposantLibelle(ligne.getComposant().getLibelle());
        dto.setComposantType(ligne.getComposant().getType());
        dto.setQuantite(ligne.getQuantite());
        return dto;
    }
}
