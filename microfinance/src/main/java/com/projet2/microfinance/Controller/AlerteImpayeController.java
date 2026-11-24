package com.projet2.microfinance.Controller;

import com.projet2.microfinance.DTO.AlerteImpayeDTO;
import com.projet2.microfinance.Service.ImpayeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alertes-impayes")
@RequiredArgsConstructor
public class AlerteImpayeController {

    private final ImpayeService impayeService;

    // F16 - POST /alertes-impayes/execution : lance la tâche nocturne tout de suite (pratique pour tester sans attendre 3h)
    @PostMapping("/execution")
    public List<AlerteImpayeDTO> executer() {
        return impayeService.detecterImpayes();
    }
}
