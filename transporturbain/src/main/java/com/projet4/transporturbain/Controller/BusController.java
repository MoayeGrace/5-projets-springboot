package com.projet4.transporturbain.Controller;

import com.projet4.transporturbain.DTO.BusRequestDTO;
import com.projet4.transporturbain.DTO.BusResponseDTO;
import com.projet4.transporturbain.Service.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bus")
@RequiredArgsConstructor
public class BusController {

    private final BusService busService;

    // GET /bus : parc de bus (200)
    @GetMapping
    public List<BusResponseDTO> lister() {
        return busService.lister();
    }

    // GET /bus/{id} : fiche d'un bus (200, ou 404)
    @GetMapping("/{id}")
    public BusResponseDTO detail(@PathVariable Long id) {
        return busService.detail(id);
    }

    // POST /bus : enregistrement d'un bus (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BusResponseDTO creer(@Valid @RequestBody BusRequestDTO dto) {
        return busService.creer(dto);
    }

    // PUT /bus/{id} : modification d'un bus (200, 400 ou 404)
    @PutMapping("/{id}")
    public BusResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody BusRequestDTO dto) {
        return busService.modifier(id, dto);
    }

    // DELETE /bus/{id} : suppression d'un bus réformé (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        busService.supprimer(id);
    }
}