package com.projet2.microfinance.Controller;

import com.projet2.microfinance.DTO.PaiementRequestDTO;
import com.projet2.microfinance.DTO.PaiementResponseDTO;
import com.projet2.microfinance.Service.PaiementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    // GET /paiements : historique de tous les paiements (200)
    @GetMapping
    public List<PaiementResponseDTO> lister() {
        return paiementService.lister();
    }

    // GET /paiements/{id} : détail d'un paiement (200, ou 404)
    @GetMapping("/{id}")
    public PaiementResponseDTO detail(@PathVariable Long id) {
        return paiementService.detail(id);
    }

    // POST /paiements : enregistrement d'un paiement reçu (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaiementResponseDTO creer(@Valid @RequestBody PaiementRequestDTO dto) {
        return paiementService.creer(dto);
    }

    // PUT /paiements/{id} : correction d'une saisie (200, 400 ou 404)
    @PutMapping("/{id}")
    public PaiementResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody PaiementRequestDTO dto) {
        return paiementService.modifier(id, dto);
    }

    // DELETE /paiements/{id} : suppression d'un paiement enregistré à tort (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        paiementService.supprimer(id);
    }
}
