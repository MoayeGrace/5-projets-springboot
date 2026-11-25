package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.DTO.ArticleRequestDTO;
import com.projet3.atelierdeproduction.DTO.ArticleResponseDTO;
import com.projet3.atelierdeproduction.DTO.PageResponseDTO;
import com.projet3.atelierdeproduction.Entity.ArticleEntity;
import com.projet3.atelierdeproduction.Repository.ArticleRepository;
import com.projet3.atelierdeproduction.Repository.LigneNomenclatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static com.projet3.atelierdeproduction.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final LigneNomenclatureRepository ligneNomenclatureRepository;

    // F10 - taille de page maximale autorisée
    private static final int TAILLE_PAGE_MAX = 100;

    // F10 - GET /articles?page=&size=&sort= : catalogue page par page (une taille > 100 est ramenée à 100)
    public PageResponseDTO<ArticleResponseDTO> lister(Pageable pageable) {
        if (pageable.getPageSize() > TAILLE_PAGE_MAX) {
            pageable = PageRequest.of(pageable.getPageNumber(), TAILLE_PAGE_MAX, pageable.getSort());
        }
        return new PageResponseDTO<>(articleRepository.findAll(pageable).map(this::entityVersResponseDTO));
    }

    // GET /articles/{id} : récupère la fiche d'un article (404 s'il n'existe pas)
    public ArticleResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(articleRepository, id, "Article"));
    }

    // POST /articles : crée un nouvel article (INSERT)
    public ArticleResponseDTO creer(ArticleRequestDTO dto) {
        ArticleEntity article = new ArticleEntity();
        dtoVersEntity(dto, article);
        return entityVersResponseDTO(articleRepository.save(article));
    }

    // PUT /articles/{id} : charge l'article (404 sinon), remplace ses champs, puis UPDATE ; la référence d'un composant
    // utilisé dans une nomenclature ne change pas (409), le stock ne descend pas sous la quantité réservée (409)
    public ArticleResponseDTO modifier(Long id, ArticleRequestDTO dto) {
        ArticleEntity article = trouverOu404(articleRepository, id, "Article");
        if (!article.getReference().equals(dto.getReference())
                && ligneNomenclatureRepository.existsByComposantReference(article.getReference())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La référence " + article.getReference() + " est utilisée dans des nomenclatures : elle ne peut pas changer");
        }
        if (dto.getQuantiteStock() != null && dto.getQuantiteStock().compareTo(article.getQuantiteReservee()) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le stock ne peut pas descendre sous la quantité réservée (" + article.getQuantiteReservee() + ")");
        }
        dtoVersEntity(dto, article);
        return entityVersResponseDTO(articleRepository.save(article));
    }

    // DELETE /articles/{id} : supprime la référence (404 si inconnue, 409 si elle sert de composant dans une nomenclature)
    public void supprimer(Long id) {
        ArticleEntity article = trouverOu404(articleRepository, id, "Article");
        if (ligneNomenclatureRepository.existsByComposantReference(article.getReference())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "L'article " + article.getReference() + " est utilisé comme composant : retirez-le d'abord des nomenclatures");
        }
        articleRepository.delete(article);
    }

    // DTO -> Entity (stock absent : 0 à la création, inchangé à la modification)
    private void dtoVersEntity(ArticleRequestDTO dto, ArticleEntity article) {
        article.setReference(dto.getReference());
        article.setLibelle(dto.getLibelle());
        article.setUnite(dto.getUnite());
        article.setType(dto.getType());
        article.setDureeUnitaireMinutes(dto.getDureeUnitaireMinutes());   // F12
        if (dto.getQuantiteStock() != null) {
            article.setQuantiteStock(dto.getQuantiteStock());             // F14
        }
    }

    // Entity -> DTO
    private ArticleResponseDTO entityVersResponseDTO(ArticleEntity article) {
        ArticleResponseDTO dto = new ArticleResponseDTO();
        dto.setId(article.getId());
        dto.setReference(article.getReference());
        dto.setLibelle(article.getLibelle());
        dto.setUnite(article.getUnite());
        dto.setType(article.getType());
        dto.setDureeUnitaireMinutes(article.getDureeUnitaireMinutes());   // F12
        dto.setQuantiteStock(article.getQuantiteStock());                 // F14
        dto.setQuantiteReservee(article.getQuantiteReservee());
        dto.setQuantiteDisponible(article.quantiteDisponible());
        return dto;
    }
}
