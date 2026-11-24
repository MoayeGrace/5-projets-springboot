package com.projet1.placedemarche.Controller;

import com.projet1.placedemarche.DTO.AlerteStockDTO;
import com.projet1.placedemarche.Service.AlerteStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alertes-stock")
@RequiredArgsConstructor
public class AlerteStockController {

    private final AlerteStockService alerteStockService;

    // F16 - POST /alertes-stock/execution : lance la tâche nocturne tout de suite (pratique pour tester sans attendre 2h)
    @PostMapping("/execution")
    public List<AlerteStockDTO> executer() {
        return alerteStockService.signalerStocksFaibles();
    }
}
