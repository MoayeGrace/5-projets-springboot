package com.projet1.placedemarche.Controller;

import com.projet1.placedemarche.DTO.VendeurRequestDTO;
import com.projet1.placedemarche.DTO.VendeurResponseDTO;
import com.projet1.placedemarche.Service.VendeurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendeurs")
@RequiredArgsConstructor
public class VendeurController {

    private final VendeurService vendeurService;

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
    @ResponseStatus(HttpStatus.CREATED)
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        vendeurService.supprimer(id);
    }
}