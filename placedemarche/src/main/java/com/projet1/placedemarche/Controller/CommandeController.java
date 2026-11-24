package com.projet1.placedemarche.Controller;

import com.projet1.placedemarche.DTO.CommandeRequestDTO;
import com.projet1.placedemarche.DTO.CommandeResponseDTO;
import com.projet1.placedemarche.Service.CommandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commandes")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;

    // GET /commandes : liste de toutes les commandes (200)
    @GetMapping
    public List<CommandeResponseDTO> lister() {
        return commandeService.lister();
    }

    // GET /commandes/{id} : détail d'une commande (200, ou 404)
    @GetMapping("/{id}")
    public CommandeResponseDTO detail(@PathVariable Long id) {
        return commandeService.detail(id);
    }

    // POST /commandes : création d'une commande (201 ; 400 JSON invalide ; 409 stock insuffisant - F11)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommandeResponseDTO creer(@Valid @RequestBody CommandeRequestDTO dto) {
        return commandeService.creer(dto);
    }

    // PUT /commandes/{id} : modification d'une commande (200 ; 400 ; 404 ; 409 stock insuffisant - F11)
    @PutMapping("/{id}")
    public CommandeResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody CommandeRequestDTO dto) {
        return commandeService.modifier(id, dto);
    }

    // DELETE /commandes/{id} : suppression d'une commande (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        commandeService.supprimer(id);
    }
}