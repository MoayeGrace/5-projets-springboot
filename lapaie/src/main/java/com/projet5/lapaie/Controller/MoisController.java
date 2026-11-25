package com.projet5.lapaie.Controller;

import com.projet5.lapaie.DTO.ClotureResponseDTO;
import com.projet5.lapaie.Service.ClotureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mois")
@RequiredArgsConstructor
public class MoisController {

    private final ClotureService clotureService;

    // F16 - POST /mois/2026-10/cloture : arrête définitivement le mois de paie (200, 400 ou 409)
    @PostMapping("/{mois}/cloture")
    public ClotureResponseDTO cloturer(@PathVariable String mois) {
        return clotureService.cloturer(mois);
    }
}
