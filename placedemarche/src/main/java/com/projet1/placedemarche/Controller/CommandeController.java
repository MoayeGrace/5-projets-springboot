package com.projet1.placedemarche.Controller;

import com.projet1.placedemarche.DTO.CommandeRequestDTO;
import com.projet1.placedemarche.DTO.CommandeResponseDTO;
import com.projet1.placedemarche.DTO.StatutRequestDTO;
import com.projet1.placedemarche.Service.CommandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    // PUT /commandes/{id} : modification d'une commande EN_ATTENTE (200 ; 400 ; 404 ; 409 si statut ou stock - F11)
    @PutMapping("/{id}")
    public CommandeResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody CommandeRequestDTO dto) {
        return commandeService.modifier(id, dto);
    }

    // DELETE /commandes/{id} : suppression d'une commande ANNULEE (204 ; 404 ; 409 si pas annulée)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        commandeService.supprimer(id);
    }

    // F13 - POST /commandes/{id}/statut { "statut": "PAYEE" } : fait évoluer le statut (200 ; 409 si transition interdite)
    @PostMapping("/{id}/statut")
    public CommandeResponseDTO changerStatut(@PathVariable Long id, @Valid @RequestBody StatutRequestDTO dto) {
        return commandeService.changerStatut(id, dto.getStatut());
    }

    // F14 - POST /commandes/{id}/annulation : annule et restitue le stock (200 ; 409 si EXPEDIEE, LIVREE ou déjà annulée)
    @PostMapping("/{id}/annulation")
    public CommandeResponseDTO annuler(@PathVariable Long id) {
        return commandeService.annuler(id);
    }

    // F19 - GET /commandes/a-expedier/export : télécharge le fichier CSV des commandes PAYEE triées par vendeur
    // ResponseEntity permet de régler nous-mêmes le type (text/csv) et l'en-tête de téléchargement
    @GetMapping("/a-expedier/export")
    public ResponseEntity<String> exporterAExpedier() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"commandes-a-expedier.csv\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(commandeService.exporterAExpedierCsv());
    }
}
