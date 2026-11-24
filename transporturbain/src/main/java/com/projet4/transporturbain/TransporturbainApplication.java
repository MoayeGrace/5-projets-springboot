package com.projet4.transporturbain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // F16 - active les tâches planifiées (@Scheduled)
public class TransporturbainApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransporturbainApplication.class, args);
    }

}
