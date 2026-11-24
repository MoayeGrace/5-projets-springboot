package com.projet2.microfinance.Controller;

import com.projet2.microfinance.DTO.EcheancierDTO;
import com.projet2.microfinance.DTO.PaiementResponseDTO;
import com.projet2.microfinance.DTO.PretRequestDTO;
import com.projet2.microfinance.DTO.PretResponseDTO;
import com.projet2.microfinance.Service.EcheanceService;
import com.projet2.microfinance.Service.PaiementService;
import com.projet2.microfinance.Service.PretService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prets")
@RequiredArgsConstructor
public class PretController {

    private final PretService pretService;
    private final EcheanceService echeanceService;   // F8
    private final PaiementService paiementService;   // F9

    // GET /prets : portefeuille de prêts (200)
    @GetMapping
    public List<PretResponseDTO> lister() {
        return pretService.lister();
    }

    // GET /prets/{id} : détail d'un prêt (200, ou 404)
    @GetMapping("/{id}")
    public PretResponseDTO detail(@PathVariable Long id) {
        return pretService.detail(id);
    }

    // F8 - GET /prets/{id}/echeances : échéancier du prêt, trié par date, avec le payé et le reste à payer (200, ou 404)
    @GetMapping("/{id}/echeances")
    public EcheancierDTO echeances(@PathVariable Long id) {
        return echeanceService.echeancierDuPret(id);
    }

    // F9 - GET /prets/{id}/paiements : paiements du prêt, du plus récent au plus ancien (200, ou 404)
    @GetMapping("/{id}/paiements")
    public List<PaiementResponseDTO> paiements(@PathVariable Long id) {
        return paiementService.listerParPret(id);
    }

    // POST /prets : enregistrement d'un prêt accordé (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PretResponseDTO creer(@Valid @RequestBody PretRequestDTO dto) {
        return pretService.creer(dto);
    }

    // PUT /prets/{id} : modification d'un prêt, dont son statut (200, 400 ou 404)
    @PutMapping("/{id}")
    public PretResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody PretRequestDTO dto) {
        return pretService.modifier(id, dto);
    }

    // DELETE /prets/{id} : suppression d'un dossier abandonné (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        pretService.supprimer(id);
    }
}
