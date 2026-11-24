package com.projet3.atelierdeproduction.Controller;

import com.projet3.atelierdeproduction.DTO.ArticleRequestDTO;
import com.projet3.atelierdeproduction.DTO.ArticleResponseDTO;
import com.projet3.atelierdeproduction.DTO.LigneNomenclatureResponseDTO;
import com.projet3.atelierdeproduction.DTO.PageResponseDTO;
import com.projet3.atelierdeproduction.Service.ArticleService;
import com.projet3.atelierdeproduction.Service.LigneNomenclatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final LigneNomenclatureService ligneNomenclatureService;   // F6

    // F10 - GET /articles?page=0&size=30&sort=reference,asc : catalogue page par page (200, ou 400 si tri invalide)
    // Pageable : Spring le remplit tout seul à partir de page, size et sort ; @PageableDefault = 1re page de 30 éléments
    @GetMapping
    public PageResponseDTO<ArticleResponseDTO> lister(@PageableDefault(size = 30) Pageable pageable) {
        return articleService.lister(pageable);
    }

    // GET /articles/{id} : fiche d'un article (200, ou 404)
    @GetMapping("/{id}")
    public ArticleResponseDTO detail(@PathVariable Long id) {
        return articleService.detail(id);
    }

    // F6 - GET /articles/{id}/nomenclature : composants directs de l'article, triés par référence (200, ou 404)
    @GetMapping("/{id}/nomenclature")
    public List<LigneNomenclatureResponseDTO> nomenclature(@PathVariable Long id) {
        return ligneNomenclatureService.composantsDirects(id);
    }

    // F7 - GET /articles/{id}/utilisations : articles dont la nomenclature contient ce composant (200, ou 404)
    @GetMapping("/{id}/utilisations")
    public List<LigneNomenclatureResponseDTO> utilisations(@PathVariable Long id) {
        return ligneNomenclatureService.utilisations(id);
    }

    // POST /articles : création d'un article (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponseDTO creer(@Valid @RequestBody ArticleRequestDTO dto) {
        return articleService.creer(dto);
    }

    // PUT /articles/{id} : modification d'un article (200, 400 ou 404)
    @PutMapping("/{id}")
    public ArticleResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody ArticleRequestDTO dto) {
        return articleService.modifier(id, dto);
    }

    // DELETE /articles/{id} : suppression d'une référence abandonnée (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        articleService.supprimer(id);
    }
}