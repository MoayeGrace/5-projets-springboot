package com.projet5.lapaie.Controller;

import com.projet5.lapaie.DTO.BulletinRequestDTO;
import com.projet5.lapaie.DTO.BulletinResponseDTO;
import com.projet5.lapaie.DTO.PageResponseDTO;
import com.projet5.lapaie.Service.BulletinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bulletins")
@RequiredArgsConstructor
public class BulletinController {

    private final BulletinService bulletinService;

    // F10 - GET /bulletins?page=0&size=50&sort=mois,desc : bulletins page par page (200, ou 400 si tri invalide)
    // Pageable : Spring le remplit tout seul à partir de page, size et sort ; @PageableDefault = 1re page de 50 éléments
    @GetMapping
    public PageResponseDTO<BulletinResponseDTO> lister(@PageableDefault(size = 50) Pageable pageable) {
        return bulletinService.lister(pageable);
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

    // F14 - POST /bulletins/{id}/edition : édite et verrouille le bulletin (200, 400, 404 ou 409)
    @PostMapping("/{id}/edition")
    public BulletinResponseDTO editer(@PathVariable Long id) {
        return bulletinService.editer(id);
    }

    // DELETE /bulletins/{id} : suppression d'un bulletin généré à tort (204, ou 404)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        bulletinService.supprimer(id);
    }
}