package com.projet1.placedemarche.Controller;

import com.projet1.placedemarche.DTO.PageResponseDTO;
import com.projet1.placedemarche.DTO.ProduitRequestDTO;
import com.projet1.placedemarche.DTO.ProduitResponseDTO;
import com.projet1.placedemarche.Service.ProduitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;

    // F9 + F10 - GET /produits?nom=casque&prixMin=5000&prixMax=50000&page=0&size=20&sort=prix,asc (200, ou 400)
    // Pageable : Spring le remplit tout seul à partir de page, size et sort ; @PageableDefault = 1re page de 20 éléments
    @GetMapping
    public PageResponseDTO<ProduitResponseDTO> rechercher(@RequestParam(required = false) String nom,
                                                          @RequestParam(required = false) BigDecimal prixMin,
                                                          @RequestParam(required = false) BigDecimal prixMax,
                                                          @PageableDefault(size = 20) Pageable pageable) {
        return produitService.rechercher(nom, prixMin, prixMax, pageable);
    }

    // GET /produits/{id} : détail d'un produit (200, ou 404)
    @GetMapping("/{id}")
    public ProduitResponseDTO detail(@PathVariable Long id) {
        return produitService.detail(id);
    }

    // POST /produits : création d'un produit (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProduitResponseDTO creer(@Valid @RequestBody ProduitRequestDTO dto) {
        return produitService.creer(dto);
    }

    // PUT /produits/{id} : modification d'un produit (200, 400 ou 404)
    @PutMapping("/{id}")
    public ProduitResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody ProduitRequestDTO dto) {
        return produitService.modifier(id, dto);
    }

    // DELETE /produits/{id} : suppression d'un produit (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        produitService.supprimer(id);
    }
}