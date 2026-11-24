package com.projet2.microfinance.Controller;

import com.projet2.microfinance.DTO.ClientRequestDTO;
import com.projet2.microfinance.DTO.ClientResponseDTO;
import com.projet2.microfinance.DTO.CompteResponseDTO;
import com.projet2.microfinance.Service.ClientService;
import com.projet2.microfinance.Service.CompteService;
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
    private final CompteService compteService;   // F6

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

    // F6 - GET /clients/{id}/comptes : comptes du client triés par date d'ouverture (200, ou 404)
    @GetMapping("/{id}/comptes")
    public List<CompteResponseDTO> comptes(@PathVariable Long id) {
        return compteService.listerParClient(id);
    }

    // POST /clients : création d'une fiche client (201, ou 400 si le JSON est invalide)
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

    // DELETE /clients/{id} : suppression d'une fiche client (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        clientService.supprimer(id);
    }
}
