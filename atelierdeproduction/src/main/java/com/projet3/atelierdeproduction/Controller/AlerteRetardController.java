package com.projet3.atelierdeproduction.Controller;

import com.projet3.atelierdeproduction.DTO.AlerteRetardDTO;
import com.projet3.atelierdeproduction.Service.AlerteRetardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alertes-retard")
@RequiredArgsConstructor
public class AlerteRetardController {

    private final AlerteRetardService alerteRetardService;

    // F16 - POST /alertes-retard/execution : lance la tâche nocturne tout de suite (pratique pour tester sans attendre 2h)
    @PostMapping("/execution")
    public List<AlerteRetardDTO> executer() {
        return alerteRetardService.signalerRetards();
    }
}
