package com.projet4.transporturbain.Controller;

import com.projet4.transporturbain.DTO.CourseRequestDTO;
import com.projet4.transporturbain.DTO.CourseResponseDTO;
import com.projet4.transporturbain.Service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // GET /courses : liste des courses (200)
    @GetMapping
    public List<CourseResponseDTO> lister() {
        return courseService.lister();
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

    // PUT /courses/{id} : modification d'une course, dont son statut (200, 400 ou 404)
    @PutMapping("/{id}")
    public CourseResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody CourseRequestDTO dto) {
        return courseService.modifier(id, dto);
    }

    // DELETE /courses/{id} : suppression d'une course (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        courseService.supprimer(id);
    }
}