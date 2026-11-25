package com.projet5.lapaie.Controller;

import com.projet5.lapaie.DTO.RubriqueRequestDTO;
import com.projet5.lapaie.DTO.RubriqueResponseDTO;
import com.projet5.lapaie.Service.RubriqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rubriques")
@RequiredArgsConstructor
public class RubriqueController {

    private final RubriqueService rubriqueService;

    // GET /rubriques : référentiel des rubriques (200)
    @GetMapping
    public List<RubriqueResponseDTO> lister() {
        return rubriqueService.lister();
    }

    // GET /rubriques/{id} : détail d'une rubrique (200, ou 404)
    @GetMapping("/{id}")
    public RubriqueResponseDTO detail(@PathVariable Long id) {
        return rubriqueService.detail(id);
    }

    // POST /rubriques : création d'une rubrique (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RubriqueResponseDTO creer(@Valid @RequestBody RubriqueRequestDTO dto) {
        return rubriqueService.creer(dto);
    }

    // PUT /rubriques/{id} : ajustement d'une rubrique (200, 400 ou 404)
    @PutMapping("/{id}")
    public RubriqueResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody RubriqueRequestDTO dto) {
        return rubriqueService.modifier(id, dto);
    }

    // DELETE /rubriques/{id} : suppression d'une rubrique obsolète (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        rubriqueService.supprimer(id);
    }
}