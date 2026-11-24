package com.projet4.transporturbain.Controller;

import com.projet4.transporturbain.DTO.CourseRequestDTO;
import com.projet4.transporturbain.DTO.CourseResponseDTO;
import com.projet4.transporturbain.DTO.PageResponseDTO;
import com.projet4.transporturbain.DTO.StatutRequestDTO;
import com.projet4.transporturbain.Service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // F10 - GET /courses?page=0&size=50&sort=heureDepart,asc : courses page par page (200, ou 400 si tri invalide)
    // Pageable : Spring le remplit tout seul à partir de page, size et sort ; @PageableDefault = 1re page de 50 éléments
    @GetMapping
    public PageResponseDTO<CourseResponseDTO> lister(@PageableDefault(size = 50) Pageable pageable) {
        return courseService.lister(pageable);
    }

    // GET /courses/{id} : détail d'une course (200, ou 404)
    @GetMapping("/{id}")
    public CourseResponseDTO detail(@PathVariable Long id) {
        return courseService.detail(id);
    }

    // POST /courses : enregistrement d'une course (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponseDTO creer(@Valid @RequestBody CourseRequestDTO dto) {
        return courseService.creer(dto);
    }

    // PUT /courses/{id} : modification d'une course, hors statut (200, 400, 404 ou 409)
    @PutMapping("/{id}")
    public CourseResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody CourseRequestDTO dto) {
        return courseService.modifier(id, dto);
    }

    // F15 - POST /courses/{id}/statut : { "statut": "TERMINEE", "heureArriveeReelle": "07:52" } (200, 400, 404 ou 409)
    @PostMapping("/{id}/statut")
    public CourseResponseDTO changerStatut(@PathVariable Long id, @Valid @RequestBody StatutRequestDTO dto) {
        return courseService.changerStatut(id, dto);
    }

    // DELETE /courses/{id} : suppression d'une course (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        courseService.supprimer(id);
    }
}