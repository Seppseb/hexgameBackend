package com.example.hexgame.tasks;

import com.example.hexgame.service.GameManagerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CleanupTask {
    private final GameManagerService manager;

    public CleanupTask(GameManagerService manager) {
        this.manager = manager;
    }

    // Runs every 10 minutes
    @Scheduled(fixedDelayString = "PT10M")
    public void cleanup() {
        manager.cleanupIdleGames(Duration.ofHours(6));
    }
}
