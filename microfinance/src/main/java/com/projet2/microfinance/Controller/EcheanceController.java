package com.projet2.microfinance.Controller;

import com.projet2.microfinance.DTO.EcheanceRequestDTO;
import com.projet2.microfinance.DTO.EcheanceResponseDTO;
import com.projet2.microfinance.DTO.ImpayeClientDTO;
import com.projet2.microfinance.Service.EcheanceService;
import com.projet2.microfinance.Service.ImpayeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/echeances")
@RequiredArgsConstructor
public class EcheanceController {

    private final EcheanceService echeanceService;
    private final ImpayeService impayeService;   // F18

    // GET /echeances : liste de toutes les échéances (200)
    @GetMapping
    public List<EcheanceResponseDTO> lister() {
        return echeanceService.lister();
    }

    // F18 - GET /echeances/impayees?date=2026-11-30 : impayés à la date (aujourd'hui par défaut), groupés par client (200)
    @GetMapping("/impayees")
    public List<ImpayeClientDTO> impayees(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return impayeService.impayesParClient(date != null ? date : LocalDate.now());
    }

    // GET /echeances/{id} : détail d'une échéance (200, ou 404)
    @GetMapping("/{id}")
    public EcheanceResponseDTO detail(@PathVariable Long id) {
        return echeanceService.detail(id);
    }

    // POST /echeances : saisie d'une échéance (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EcheanceResponseDTO creer(@Valid @RequestBody EcheanceRequestDTO dto) {
        return echeanceService.creer(dto);
    }

    // PUT /echeances/{id} : correction d'une échéance (200, 400 ou 404)
    @PutMapping("/{id}")
    public EcheanceResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody EcheanceRequestDTO dto) {
        return echeanceService.modifier(id, dto);
    }

    // DELETE /echeances/{id} : suppression d'une échéance erronée (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        echeanceService.supprimer(id);
    }
}
