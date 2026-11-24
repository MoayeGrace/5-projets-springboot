package com.projet1.placedemarche.Controller;

import com.projet1.placedemarche.DTO.CategorieRequestDTO;
import com.projet1.placedemarche.DTO.CategorieResponseDTO;
import com.projet1.placedemarche.DTO.ProduitResponseDTO;
import com.projet1.placedemarche.Service.CategorieService;
import com.projet1.placedemarche.Service.ProduitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategorieController {

    private final CategorieService categorieService;
    private final ProduitService produitService;

    // GET /categories : liste de toutes les catégories (200)
    @GetMapping
    public List<CategorieResponseDTO> lister() {
        return categorieService.lister();
    }

    // GET /categories/{id} : détail d'une catégorie (200, ou 404)
    @GetMapping("/{id}")
    public CategorieResponseDTO detail(@PathVariable Long id) {
        return categorieService.detail(id);
    }

    // POST /categories : création d'une catégorie (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategorieResponseDTO creer(@Valid @RequestBody CategorieRequestDTO dto) {
        return categorieService.creer(dto);
    }

    // PUT /categories/{id} : modification d'une catégorie (200, 400 ou 404)
    @PutMapping("/{id}")
    public CategorieResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody CategorieRequestDTO dto) {
        return categorieService.modifier(id, dto);
    }

    // DELETE /categories/{id} : suppression d'une catégorie (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        categorieService.supprimer(id);
    }

    // F7 - GET /categories/{id}/produits : produits de la catégorie triés par prix, ruptures signalées (200, ou 404)
    @GetMapping("/{id}/produits")
    public List<ProduitResponseDTO> produits(@PathVariable Long id) {
        return produitService.listerParCategorie(id);
    }
}