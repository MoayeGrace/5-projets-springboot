package com.projet2.microfinance.Controller;

import com.projet2.microfinance.DTO.CompteRequestDTO;
import com.projet2.microfinance.DTO.CompteResponseDTO;
import com.projet2.microfinance.Service.CompteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comptes")
@RequiredArgsConstructor
public class CompteController {

    private final CompteService compteService;

    // GET /comptes : liste de tous les comptes (200)
    @GetMapping
    public List<CompteResponseDTO> lister() {
        return compteService.lister();
    }

    // GET /comptes/{id} : détail d'un compte (200, ou 404)
    @GetMapping("/{id}")
    public CompteResponseDTO detail(@PathVariable Long id) {
        return compteService.detail(id);
    }

    // POST /comptes : ouverture d'un compte (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompteResponseDTO creer(@Valid @RequestBody CompteRequestDTO dto) {
        return compteService.creer(dto);
    }

    // PUT /comptes/{id} : modification d'un compte (200, 400 ou 404)
    @PutMapping("/{id}")
    public CompteResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody CompteRequestDTO dto) {
        return compteService.modifier(id, dto);
    }

    // DELETE /comptes/{id} : suppression d'un compte ouvert par erreur (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        compteService.supprimer(id);
    }
}
