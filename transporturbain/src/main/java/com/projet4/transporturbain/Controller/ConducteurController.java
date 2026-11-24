package com.projet4.transporturbain.Controller;

import com.projet4.transporturbain.DTO.ConducteurRequestDTO;
import com.projet4.transporturbain.DTO.ConducteurResponseDTO;
import com.projet4.transporturbain.DTO.CourseResponseDTO;
import com.projet4.transporturbain.Service.ConducteurService;
import com.projet4.transporturbain.Service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/conducteurs")
@RequiredArgsConstructor
public class ConducteurController {

    private final ConducteurService conducteurService;
    private final CourseService courseService;   // F8

    // F8 - GET /conducteurs/{id}/courses?du=2026-11-23&au=2026-11-29 : programme du conducteur (200, 400 ou 404)
    @GetMapping("/{id}/courses")
    public List<CourseResponseDTO> courses(@PathVariable Long id,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        return courseService.listerParConducteur(id, du, au);
    }

    // GET /conducteurs : effectif des conducteurs (200)
    @GetMapping
    public List<ConducteurResponseDTO> lister() {
        return conducteurService.lister();
    }

    // GET /conducteurs/{id} : fiche d'un conducteur (200, ou 404)
    @GetMapping("/{id}")
    public ConducteurResponseDTO detail(@PathVariable Long id) {
        return conducteurService.detail(id);
    }

    // POST /conducteurs : création d'une fiche conducteur (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConducteurResponseDTO creer(@Valid @RequestBody ConducteurRequestDTO dto) {
        return conducteurService.creer(dto);
    }

    // PUT /conducteurs/{id} : modification d'un conducteur (200, 400 ou 404)
    @PutMapping("/{id}")
    public ConducteurResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody ConducteurRequestDTO dto) {
        return conducteurService.modifier(id, dto);
    }

    // DELETE /conducteurs/{id} : suppression de la fiche d'un conducteur parti (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        conducteurService.supprimer(id);
    }
}