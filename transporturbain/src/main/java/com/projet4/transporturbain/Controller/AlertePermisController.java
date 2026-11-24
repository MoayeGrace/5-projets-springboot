package com.projet4.transporturbain.Controller;

import com.projet4.transporturbain.DTO.AlertePermisDTO;
import com.projet4.transporturbain.Service.AlertePermisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alertes-permis")
@RequiredArgsConstructor
public class AlertePermisController {

    private final AlertePermisService alertePermisService;

    // F16 - POST /alertes-permis/execution : lance la tâche nocturne tout de suite (pratique pour tester sans attendre 2h)
    @PostMapping("/execution")
    public List<AlertePermisDTO> executer() {
        return alertePermisService.signalerPermis();
    }
}
