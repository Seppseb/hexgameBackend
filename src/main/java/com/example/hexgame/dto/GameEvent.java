package com.example.hexgame.dto;

public class GameEvent {
    private String type;    // e.g. "GAME_STARTED"
    private String gameId;
    private String message;

    public GameEvent() {}
    public GameEvent(String type, String gameId, String message) {
        this.type = type; this.gameId = gameId; this.message = message;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getGameId() {
        return gameId;
    }
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
