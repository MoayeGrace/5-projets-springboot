package com.projet3.atelierdeproduction.Controller;

import com.projet3.atelierdeproduction.DTO.OperateurRequestDTO;
import com.projet3.atelierdeproduction.DTO.OperateurResponseDTO;
import com.projet3.atelierdeproduction.Service.OperateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operateurs")
@RequiredArgsConstructor
public class OperateurController {

    private final OperateurService operateurService;

    // GET /operateurs : effectif des opérateurs (200)
    @GetMapping
    public List<OperateurResponseDTO> lister() {
        return operateurService.lister();
    }

    // GET /operateurs/{id} : fiche d'un opérateur (200, ou 404)
    @GetMapping("/{id}")
    public OperateurResponseDTO detail(@PathVariable Long id) {
        return operateurService.detail(id);
    }

    // POST /operateurs : création d'une fiche opérateur (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OperateurResponseDTO creer(@Valid @RequestBody OperateurRequestDTO dto) {
        return operateurService.creer(dto);
    }

    // PUT /operateurs/{id} : modification d'un opérateur (200, 400 ou 404)
    @PutMapping("/{id}")
    public OperateurResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody OperateurRequestDTO dto) {
        return operateurService.modifier(id, dto);
    }

    // DELETE /operateurs/{id} : suppression de la fiche d'un opérateur parti (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        operateurService.supprimer(id);
    }
}