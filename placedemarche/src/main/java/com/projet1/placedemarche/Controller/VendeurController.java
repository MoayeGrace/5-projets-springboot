package com.projet1.placedemarche.Controller;

import java.time.YearMonth;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.projet1.placedemarche.DTO.ChiffreAffairesDTO;
import com.projet1.placedemarche.DTO.ProduitResponseDTO;
import com.projet1.placedemarche.DTO.VendeurRequestDTO;
import com.projet1.placedemarche.DTO.VendeurResponseDTO;
import com.projet1.placedemarche.Service.ProduitService;
import com.projet1.placedemarche.Service.StatistiqueService;
import com.projet1.placedemarche.Service.VendeurService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vendeurs")
@RequiredArgsConstructor
public class VendeurController {

    private final VendeurService vendeurService;
    private final ProduitService produitService;
    private final StatistiqueService statistiqueService;   // F17

    // GET /vendeurs : liste de tous les vendeurs (200)
    @GetMapping
    public List<VendeurResponseDTO> lister() {
        return vendeurService.lister();
    }

    // GET /vendeurs/{id} : détail d'un vendeur (200, ou 404)
    @GetMapping("/{id}")
    public VendeurResponseDTO detail(@PathVariable Long id) {
        return vendeurService.detail(id);
    }

    // POST /vendeurs : création d'un vendeur (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  //201
    public VendeurResponseDTO creer(@Valid @RequestBody VendeurRequestDTO dto) {
        return vendeurService.creer(dto);
    }

    // PUT /vendeurs/{id} : modification d'un vendeur (200, 400 ou 404)
    @PutMapping("/{id}")
    public VendeurResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody VendeurRequestDTO dto) {
        return vendeurService.modifier(id, dto);
    }

    // DELETE /vendeurs/{id} : suppression d'un vendeur (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void supprimer(@PathVariable Long id) {
        vendeurService.supprimer(id);
    }

    // F17 - GET /vendeurs/{id}/chiffre-affaires?mois=2026-11 : CA du mois (commandes livrées),
    //       détail par produit (200, ou 404)
    @GetMapping("/{id}/chiffre-affaires")
    public ChiffreAffairesDTO chiffreAffaires(@PathVariable Long id,
                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mois) {
        return statistiqueService.chiffreAffaires(id, mois);
    }

    // F6 - GET /vendeurs/{id}/produits : catalogue d'un vendeur trié par nom (200, ou 404)
    @GetMapping("/{id}/produits")
    public List<ProduitResponseDTO> produits(@PathVariable Long id) {
        return produitService.listerParVendeur(id);
    }
}