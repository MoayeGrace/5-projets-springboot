package com.projet5.lapaie.Controller;

import com.projet5.lapaie.DTO.AbsenceRequestDTO;
import com.projet5.lapaie.DTO.AbsenceResponseDTO;
import com.projet5.lapaie.Service.AbsenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/absences")
@RequiredArgsConstructor
public class AbsenceController {

    private final AbsenceService absenceService;

    // GET /absences : liste des absences (200)
    @GetMapping
    public List<AbsenceResponseDTO> lister() {
        return absenceService.lister();
    }

    // GET /absences/{id} : détail d'une absence (200, ou 404)
    @GetMapping("/{id}")
    public AbsenceResponseDTO detail(@PathVariable Long id) {
        return absenceService.detail(id);
    }

    // POST /absences : saisie d'une absence (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbsenceResponseDTO creer(@Valid @RequestBody AbsenceRequestDTO dto) {
        return absenceService.creer(dto);
    }

    // PUT /absences/{id} : correction d'une absence (200, 400 ou 404)
    @PutMapping("/{id}")
    public AbsenceResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody AbsenceRequestDTO dto) {
        return absenceService.modifier(id, dto);
    }

    // DELETE /absences/{id} : suppression d'une absence saisie par erreur (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        absenceService.supprimer(id);
    }
}