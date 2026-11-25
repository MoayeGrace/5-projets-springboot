package com.projet5.lapaie.Controller;

import com.projet5.lapaie.DTO.BulletinRequestDTO;
import com.projet5.lapaie.DTO.BulletinResponseDTO;
import com.projet5.lapaie.Service.BulletinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bulletins")
@RequiredArgsConstructor
public class BulletinController {

    private final BulletinService bulletinService;

    // GET /bulletins : liste des bulletins (200)
    @GetMapping
    public List<BulletinResponseDTO> lister() {
        return bulletinService.lister();
    }

    // GET /bulletins/{id} : détail d'un bulletin (200, ou 404)
    @GetMapping("/{id}")
    public BulletinResponseDTO detail(@PathVariable Long id) {
        return bulletinService.detail(id);
    }

    // POST /bulletins : établissement d'un bulletin (201, ou 400 si le JSON est invalide)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BulletinResponseDTO creer(@Valid @RequestBody BulletinRequestDTO dto) {
        return bulletinService.creer(dto);
    }

    // PUT /bulletins/{id} : correction d'un montant (200, 400 ou 404)
    @PutMapping("/{id}")
    public BulletinResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody BulletinRequestDTO dto) {
        return bulletinService.modifier(id, dto);
    }

    // DELETE /bulletins/{id} : suppression d'un bulletin généré à tort (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        bulletinService.supprimer(id);
    }
}