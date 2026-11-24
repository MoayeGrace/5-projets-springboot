package com.projet4.transporturbain.Controller;

import com.projet4.transporturbain.DTO.CourseResponseDTO;
import com.projet4.transporturbain.DTO.DesserteRequestDTO;
import com.projet4.transporturbain.DTO.DesserteResponseDTO;
import com.projet4.transporturbain.DTO.LigneRequestDTO;
import com.projet4.transporturbain.DTO.LigneResponseDTO;
import com.projet4.transporturbain.Service.CourseService;
import com.projet4.transporturbain.Service.DesserteService;
import com.projet4.transporturbain.Service.LigneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/lignes")
@RequiredArgsConstructor
public class LigneController {

    private final LigneService ligneService;
    private final DesserteService desserteService;   // F6
    private final CourseService courseService;       // F7

    // F11 - POST /lignes/{id}/arrets : ajoute un arrêt au tracé, à une position et dans un sens (201, 400, 404 ou 409)
    @PostMapping("/{id}/arrets")
    @ResponseStatus(HttpStatus.CREATED)
    public List<DesserteResponseDTO> ajouterArret(@PathVariable Long id, @Valid @RequestBody DesserteRequestDTO dto) {
        return desserteService.ajouterArret(id, dto);
    }

    // F7 - GET /lignes/{id}/courses?date=2026-11-15 : service de la ligne sur une journée, aujourd'hui par défaut (200, ou 404)
    @GetMapping("/{id}/courses")
    public List<CourseResponseDTO> courses(@PathVariable Long id,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return courseService.listerParLigne(id, date);
    }

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