package com.projet1.placedemarche.Controller;

import com.projet1.placedemarche.DTO.ProduitRequestDTO;
import com.projet1.placedemarche.DTO.ProduitResponseDTO;
import com.projet1.placedemarche.Service.ProduitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;

    // GET /produits : liste de tous les produits (200)
    @GetMapping
    public List<ProduitResponseDTO> lister() {
        return produitService.lister();
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