package com.projet1.placedemarche.Controller;

import com.projet1.placedemarche.DTO.ClientRequestDTO;
import com.projet1.placedemarche.DTO.ClientResponseDTO;
import com.projet1.placedemarche.DTO.CommandeResponseDTO;
import com.projet1.placedemarche.Service.ClientService;
import com.projet1.placedemarche.Service.CommandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final CommandeService commandeService;

    // F8 - GET /clients/{id}/commandes : historique des commandes du client, plus récentes d'abord (200, ou 404)
    @GetMapping("/{id}/commandes")
    public List<CommandeResponseDTO> commandes(@PathVariable Long id) {
        return commandeService.listerParClient(id);
    }

    // GET /clients : liste de tous les clients (200)
    @GetMapping
    public List<ClientResponseDTO> lister() {
        return clientService.lister();
    }

    // GET /clients/{id} : détail d'un client (200, ou 404)
    @GetMapping("/{id}")
    public ClientResponseDTO detail(@PathVariable Long id) {
        return clientService.detail(id);
    }

    // POST /clients : création d'un client (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponseDTO creer(@Valid @RequestBody ClientRequestDTO dto) {
        return clientService.creer(dto);
    }

    // PUT /clients/{id} : modification d'un client (200, 400 ou 404)
    @PutMapping("/{id}")
    public ClientResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody ClientRequestDTO dto) {
        return clientService.modifier(id, dto);
    }

    // DELETE /clients/{id} : suppression d'un client (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        clientService.supprimer(id);
    }
}