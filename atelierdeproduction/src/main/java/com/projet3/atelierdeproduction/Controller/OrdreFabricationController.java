package com.projet3.atelierdeproduction.Controller;

import com.projet3.atelierdeproduction.DTO.OrdreFabricationRequestDTO;
import com.projet3.atelierdeproduction.DTO.OrdreFabricationResponseDTO;
import com.projet3.atelierdeproduction.Service.OrdreFabricationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordres-fabrication")
@RequiredArgsConstructor
public class OrdreFabricationController {

    private final OrdreFabricationService ordreFabricationService;

    // GET /ordres-fabrication : carnet des ordres de fabrication (200)
    @GetMapping
    public List<OrdreFabricationResponseDTO> lister() {
        return ordreFabricationService.lister();
    }

    // GET /ordres-fabrication/{id} : détail d'un ordre (200, ou 404)
    @GetMapping("/{id}")
    public OrdreFabricationResponseDTO detail(@PathVariable Long id) {
        return ordreFabricationService.detail(id);
    }

    // POST /ordres-fabrication : lancement d'un ordre de fabrication (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdreFabricationResponseDTO creer(@Valid @RequestBody OrdreFabricationRequestDTO dto) {
        return ordreFabricationService.creer(dto);
    }

    // PUT /ordres-fabrication/{id} : modification d'un ordre, dont son statut (200, 400 ou 404)
    @PutMapping("/{id}")
    public OrdreFabricationResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody OrdreFabricationRequestDTO dto) {
        return ordreFabricationService.modifier(id, dto);
    }

    // DELETE /ordres-fabrication/{id} : suppression d'un ordre (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        ordreFabricationService.supprimer(id);
    }
}