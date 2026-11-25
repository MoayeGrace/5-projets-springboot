package com.projet5.lapaie.Controller;

import com.projet5.lapaie.DTO.AbsenceResponseDTO;
import com.projet5.lapaie.DTO.BulletinResponseDTO;
import com.projet5.lapaie.DTO.ContratResponseDTO;
import com.projet5.lapaie.DTO.EmployeRequestDTO;
import com.projet5.lapaie.DTO.EmployeResponseDTO;
import com.projet5.lapaie.Service.AbsenceService;
import com.projet5.lapaie.Service.BulletinService;
import com.projet5.lapaie.Service.ContratService;
import com.projet5.lapaie.Service.EmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/employes")
@RequiredArgsConstructor
public class EmployeController {

    private final EmployeService employeService;
    private final ContratService contratService;   // F6
    private final BulletinService bulletinService; // F7
    private final AbsenceService absenceService;   // F8

    // F8 - GET /employes/{id}/absences?du=2026-11-01&au=2026-11-30 : absences de la période, mois en cours par défaut (200, 400 ou 404)
    @GetMapping("/{id}/absences")
    public List<AbsenceResponseDTO> absences(@PathVariable Long id,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        return absenceService.listerParEmploye(id, du, au);
    }

    // F7 - GET /employes/{id}/bulletins?annee=2026 : bulletins de l'année par mois décroissant, avec l'état d'édition (200, ou 404)
    @GetMapping("/{id}/bulletins")
    public List<BulletinResponseDTO> bulletins(@PathVariable Long id, @RequestParam(required = false) Integer annee) {
        return bulletinService.listerParEmploye(id, annee);
    }

    // F6 - GET /employes/{id}/contrats : contrats du plus récent au plus ancien, le contrat actif signalé (200, ou 404)
    @GetMapping("/{id}/contrats")
    public List<ContratResponseDTO> contrats(@PathVariable Long id) {
        return contratService.listerParEmploye(id);
    }

    // F9 - GET /employes?matricule=EMP00&service=TECHNIQUE : recherche par critères facultatifs et combinables (200)
    @GetMapping
    public List<EmployeResponseDTO> rechercher(@RequestParam(required = false) String matricule,
                                               @RequestParam(required = false) String service) {
        return employeService.rechercher(matricule, service);
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