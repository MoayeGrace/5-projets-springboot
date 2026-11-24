package com.projet1.placedemarche;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // F16 - active les tâches planifiées (@Scheduled)
public class PlacedemarcheApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlacedemarcheApplication.class, args);
    }

}
