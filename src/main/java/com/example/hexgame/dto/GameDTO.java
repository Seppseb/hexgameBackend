package com.example.hexgame.dto;

import java.time.Instant;
import java.util.Map;

import com.example.hexgame.model.Bank;
import com.example.hexgame.model.Board;
import com.example.hexgame.model.GameState;
import com.example.hexgame.model.TradeOffer;

public class GameDTO {
    public String id;
    public GameState state;
    public Instant lastActive;
    public String ownerId;

    public Board board;
    public Bank bank;
    public Map<String, PlayerInfoDTO> players;
    public PlayerInfoDTO currentPlayer;

    public TradeOffer currentTradeOffer;
    public boolean isWaitingForMovingRobber;
    public boolean isInitialIsPlacingRoad;

    public PlayerDTO you;
}
