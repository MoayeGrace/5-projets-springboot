package com.projet3.atelierdeproduction.Controller;

import com.projet3.atelierdeproduction.DTO.MachineRequestDTO;
import com.projet3.atelierdeproduction.DTO.MachineResponseDTO;
import com.projet3.atelierdeproduction.DTO.ChargeMachineDTO;
import com.projet3.atelierdeproduction.DTO.MaintenanceRequestDTO;
import com.projet3.atelierdeproduction.DTO.MaintenanceResponseDTO;
import com.projet3.atelierdeproduction.DTO.OrdreFabricationResponseDTO;
import com.projet3.atelierdeproduction.Service.ChargeMachineService;
import com.projet3.atelierdeproduction.Service.MachineService;
import com.projet3.atelierdeproduction.Service.OrdreFabricationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;
    private final OrdreFabricationService ordreFabricationService;   // F9
    private final ChargeMachineService chargeMachineService;         // F19

    // F19 - GET /machines/{id}/charge?du=2026-11-23&au=2026-11-27 : taux d'utilisation, nombre d'ordres, détail par jour (200, 400 ou 404)
    @GetMapping("/{id}/charge")
    public ChargeMachineDTO charge(@PathVariable Long id,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        return chargeMachineService.charge(id, du, au);
    }

    // F19 - POST /machines/{id}/maintenances : enregistre un arrêt de maintenance (201, 400 ou 404)
    @PostMapping("/{id}/maintenances")
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceResponseDTO ajouterMaintenance(@PathVariable Long id, @Valid @RequestBody MaintenanceRequestDTO dto) {
        return chargeMachineService.ajouterMaintenance(id, dto);
    }

    // F19 - GET /machines/{id}/maintenances : arrêts de maintenance de la machine (200, ou 404)
    @GetMapping("/{id}/maintenances")
    public List<MaintenanceResponseDTO> maintenances(@PathVariable Long id) {
        return chargeMachineService.maintenances(id);
    }

    // F9 - GET /machines/{id}/ordres?du=2026-11-23&au=2026-11-29 : programme de la machine, semaine en cours par défaut (200, 400 ou 404)
    @GetMapping("/{id}/ordres")
    public List<OrdreFabricationResponseDTO> ordres(@PathVariable Long id,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        return ordreFabricationService.listerParMachine(id, du, au);
    }

    // GET /machines : parc de machines (200)
    @GetMapping
    public List<MachineResponseDTO> lister() {
        return machineService.lister();
    }

    // GET /machines/{id} : fiche d'une machine (200, ou 404)
    @GetMapping("/{id}")
    public MachineResponseDTO detail(@PathVariable Long id) {
        return machineService.detail(id);
    }

    // POST /machines : enregistrement d'une machine (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MachineResponseDTO creer(@Valid @RequestBody MachineRequestDTO dto) {
        return machineService.creer(dto);
    }

    // PUT /machines/{id} : modification d'une machine (200, 400 ou 404)
    @PutMapping("/{id}")
    public MachineResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody MachineRequestDTO dto) {
        return machineService.modifier(id, dto);
    }

    // DELETE /machines/{id} : suppression d'une machine réformée (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        machineService.supprimer(id);
    }
}