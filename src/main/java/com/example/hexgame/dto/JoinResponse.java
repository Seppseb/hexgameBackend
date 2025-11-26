package com.example.hexgame.dto;

public class JoinResponse {
    private String userId;
    private String gameId;
    private String message;

    public JoinResponse(String userId, String gameId, String message) {
        this.userId = userId; this.gameId = gameId; this.message = message;
    }
    // getters/setters
}
