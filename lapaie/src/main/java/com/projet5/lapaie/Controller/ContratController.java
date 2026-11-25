package com.projet5.lapaie.Controller;

import com.projet5.lapaie.DTO.ContratRequestDTO;
import com.projet5.lapaie.DTO.ContratResponseDTO;
import com.projet5.lapaie.Service.ContratService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contrats")
@RequiredArgsConstructor
public class ContratController {

    private final ContratService contratService;

    // GET /contrats : liste des contrats (200)
    @GetMapping
    public List<ContratResponseDTO> lister() {
        return contratService.lister();
    }

    // GET /contrats/{id} : détail d'un contrat (200, ou 404)
    @GetMapping("/{id}")
    public ContratResponseDTO detail(@PathVariable Long id) {
        return contratService.detail(id);
    }

    // POST /contrats : enregistrement d'un contrat (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratResponseDTO creer(@Valid @RequestBody ContratRequestDTO dto) {
        return contratService.creer(dto);
    }

    // PUT /contrats/{id} : modification d'un contrat (augmentation...) (200, 400 ou 404)
    @PutMapping("/{id}")
    public ContratResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody ContratRequestDTO dto) {
        return contratService.modifier(id, dto);
    }

    // DELETE /contrats/{id} : suppression d'un contrat saisi par erreur (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        contratService.supprimer(id);
    }
}