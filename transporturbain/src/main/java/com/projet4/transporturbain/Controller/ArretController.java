package com.projet4.transporturbain.Controller;

import com.projet4.transporturbain.DTO.ArretRequestDTO;
import com.projet4.transporturbain.DTO.ArretResponseDTO;
import com.projet4.transporturbain.Service.ArretService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/arrets")
@RequiredArgsConstructor
public class ArretController {

    private final ArretService arretService;

    // F9 - GET /arrets?commune=Cocody : arrêts de la commune triés par nom, avec les lignes qui les desservent (200)
    @GetMapping
    public List<ArretResponseDTO> lister(@RequestParam(required = false) String commune) {
        return arretService.lister(commune);
    }

    // GET /arrets/{id} : détail d'un arrêt (200, ou 404)
    @GetMapping("/{id}")
    public ArretResponseDTO detail(@PathVariable Long id) {
        return arretService.detail(id);
    }

    // POST /arrets : enregistrement d'un arrêt (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArretResponseDTO creer(@Valid @RequestBody ArretRequestDTO dto) {
        return arretService.creer(dto);
    }

    // PUT /arrets/{id} : correction d'un arrêt (200, 400 ou 404)
    @PutMapping("/{id}")
    public ArretResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody ArretRequestDTO dto) {
        return arretService.modifier(id, dto);
    }

    // DELETE /arrets/{id} : suppression d'un arrêt désaffecté (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        arretService.supprimer(id);
    }
}