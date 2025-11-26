package com.example.hexgame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HexgameBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(HexgameBackendApplication.class, args);
    }
}
