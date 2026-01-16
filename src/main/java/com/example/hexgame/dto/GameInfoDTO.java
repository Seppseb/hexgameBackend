package com.example.hexgame.dto;

import java.time.Instant;
import java.util.Map;

import com.example.hexgame.model.GameState;

public class GameInfoDTO {

    public String id;
    public GameState state;
    public Instant lastActive;
    public String ownerId;

    public PlayerInfoDTO winner;

    public Map<String, PlayerInfoDTO> players;
}