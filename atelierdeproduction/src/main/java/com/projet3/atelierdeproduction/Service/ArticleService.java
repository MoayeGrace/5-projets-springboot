package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.DTO.ArticleRequestDTO;
import com.projet3.atelierdeproduction.DTO.ArticleResponseDTO;
import com.projet3.atelierdeproduction.DTO.PageResponseDTO;
import com.projet3.atelierdeproduction.Entity.ArticleEntity;
import com.projet3.atelierdeproduction.Repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.projet3.atelierdeproduction.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

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

    // PUT /articles/{id} : charge l'article existant (404 sinon), remplace ses champs, puis UPDATE
    public ArticleResponseDTO modifier(Long id, ArticleRequestDTO dto) {
        ArticleEntity article = trouverOu404(articleRepository, id, "Article");
        dtoVersEntity(dto, article);
        return entityVersResponseDTO(articleRepository.save(article));
    }

    // DELETE /articles/{id} : vérifie que l'article existe (404 sinon), puis supprime la référence
    public void supprimer(Long id) {
        articleRepository.delete(trouverOu404(articleRepository, id, "Article"));
    }

    // DTO -> Entity
    private void dtoVersEntity(ArticleRequestDTO dto, ArticleEntity article) {
        article.setReference(dto.getReference());
        article.setLibelle(dto.getLibelle());
        article.setUnite(dto.getUnite());
        article.setType(dto.getType());
        article.setTempsUnitaireMinutes(dto.getTempsUnitaireMinutes() != null ? dto.getTempsUnitaireMinutes() : 0);   // F12
    }

    // Entity -> DTO
    private ArticleResponseDTO entityVersResponseDTO(ArticleEntity article) {
        ArticleResponseDTO dto = new ArticleResponseDTO();
        dto.setId(article.getId());
        dto.setReference(article.getReference());
        dto.setLibelle(article.getLibelle());
        dto.setUnite(article.getUnite());
        dto.setType(article.getType());
        dto.setTempsUnitaireMinutes(article.getTempsUnitaireMinutes());   // F12
        return dto;
    }
}
