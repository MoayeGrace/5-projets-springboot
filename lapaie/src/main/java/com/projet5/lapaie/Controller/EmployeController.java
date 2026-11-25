package com.projet5.lapaie.Controller;

import com.projet5.lapaie.DTO.ContratResponseDTO;
import com.projet5.lapaie.DTO.EmployeRequestDTO;
import com.projet5.lapaie.DTO.EmployeResponseDTO;
import com.projet5.lapaie.Service.ContratService;
import com.projet5.lapaie.Service.EmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employes")
@RequiredArgsConstructor
public class EmployeController {

    private final EmployeService employeService;
    private final ContratService contratService;   // F6

    // F6 - GET /employes/{id}/contrats : contrats du plus récent au plus ancien, le contrat actif signalé (200, ou 404)
    @GetMapping("/{id}/contrats")
    public List<ContratResponseDTO> contrats(@PathVariable Long id) {
        return contratService.listerParEmploye(id);
    }

    // GET /employes : effectif (200)
    @GetMapping
    public List<EmployeResponseDTO> lister() {
        return employeService.lister();
    }

    // GET /employes/{id} : fiche d'un employé (200, ou 404)
    @GetMapping("/{id}")
    public EmployeResponseDTO detail(@PathVariable Long id) {
        return employeService.detail(id);
    }

    // POST /employes : création d'une fiche employé (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeResponseDTO creer(@Valid @RequestBody EmployeRequestDTO dto) {
        return employeService.creer(dto);
    }

    // PUT /employes/{id} : modification d'un employé (200, 400 ou 404)
    @PutMapping("/{id}")
    public EmployeResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody EmployeRequestDTO dto) {
        return employeService.modifier(id, dto);
    }

    // DELETE /employes/{id} : suppression de la fiche d'un employé parti (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        employeService.supprimer(id);
    }
}