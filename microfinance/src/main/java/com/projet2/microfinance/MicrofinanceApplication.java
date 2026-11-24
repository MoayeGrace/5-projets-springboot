package com.projet2.microfinance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // F16 - active les tâches planifiées (@Scheduled)
public class MicrofinanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicrofinanceApplication.class, args);
    }

}
