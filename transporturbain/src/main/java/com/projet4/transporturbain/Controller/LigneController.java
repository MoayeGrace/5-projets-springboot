package com.projet4.transporturbain.Controller;

import com.projet4.transporturbain.DTO.DesserteResponseDTO;
import com.projet4.transporturbain.DTO.LigneRequestDTO;
import com.projet4.transporturbain.DTO.LigneResponseDTO;
import com.projet4.transporturbain.Service.DesserteService;
import com.projet4.transporturbain.Service.LigneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lignes")
@RequiredArgsConstructor
public class LigneController {

    private final LigneService ligneService;
    private final DesserteService desserteService;   // F6

    // F6 - GET /lignes/{id}/arrets : tracé de la ligne, arrêts dans l'ordre de desserte (200, ou 404)
    @GetMapping("/{id}/arrets")
    public List<DesserteResponseDTO> arrets(@PathVariable Long id) {
        return desserteService.arretsDeLaLigne(id);
    }

    // GET /lignes : réseau des lignes (200)
    @GetMapping
    public List<LigneResponseDTO> lister() {
        return ligneService.lister();
    }

    // GET /lignes/{id} : détail d'une ligne (200, ou 404)
    @GetMapping("/{id}")
    public LigneResponseDTO detail(@PathVariable Long id) {
        return ligneService.detail(id);
    }

    // POST /lignes : création d'une ligne (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LigneResponseDTO creer(@Valid @RequestBody LigneRequestDTO dto) {
        return ligneService.creer(dto);
    }

    // PUT /lignes/{id} : modification d'une ligne (200, 400 ou 404)
    @PutMapping("/{id}")
    public LigneResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody LigneRequestDTO dto) {
        return ligneService.modifier(id, dto);
    }

    // DELETE /lignes/{id} : suppression d'une ligne fermée (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        ligneService.supprimer(id);
    }
}