package com.projet3.atelierdeproduction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // F16 - active les tâches planifiées (@Scheduled)
public class AtelierdeproductionApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtelierdeproductionApplication.class, args);
    }

}
