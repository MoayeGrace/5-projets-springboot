package com.projet3.atelierdeproduction.Controller;

import com.projet3.atelierdeproduction.DTO.LigneNomenclatureRequestDTO;
import com.projet3.atelierdeproduction.DTO.LigneNomenclatureResponseDTO;
import com.projet3.atelierdeproduction.Service.LigneNomenclatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomenclatures")
@RequiredArgsConstructor
public class LigneNomenclatureController {

    private final LigneNomenclatureService ligneNomenclatureService;

    // GET /nomenclatures : liste des lignes de nomenclature (200)
    @GetMapping
    public List<LigneNomenclatureResponseDTO> lister() {
        return ligneNomenclatureService.lister();
    }

    // GET /nomenclatures/{id} : détail d'une ligne (200, ou 404)
    @GetMapping("/{id}")
    public LigneNomenclatureResponseDTO detail(@PathVariable Long id) {
        return ligneNomenclatureService.detail(id);
    }

    // POST /nomenclatures : saisie d'une ligne de nomenclature (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LigneNomenclatureResponseDTO creer(@Valid @RequestBody LigneNomenclatureRequestDTO dto) {
        return ligneNomenclatureService.creer(dto);
    }

    // PUT /nomenclatures/{id} : ajustement d'une ligne (200, 400 ou 404)
    @PutMapping("/{id}")
    public LigneNomenclatureResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody LigneNomenclatureRequestDTO dto) {
        return ligneNomenclatureService.modifier(id, dto);
    }

    // DELETE /nomenclatures/{id} : suppression d'une ligne obsolète (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        ligneNomenclatureService.supprimer(id);
    }
}