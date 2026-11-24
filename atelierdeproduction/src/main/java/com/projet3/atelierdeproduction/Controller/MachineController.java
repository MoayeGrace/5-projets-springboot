package com.projet3.atelierdeproduction.Controller;

import com.projet3.atelierdeproduction.DTO.MachineRequestDTO;
import com.projet3.atelierdeproduction.DTO.MachineResponseDTO;
import com.projet3.atelierdeproduction.Service.MachineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;

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