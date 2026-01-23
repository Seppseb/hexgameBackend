package com.example.hexgame.dto;

import java.util.ArrayDeque;
import java.util.HashMap;

import com.example.hexgame.model.DevelopmentItem;
import com.example.hexgame.model.Player;
import com.example.hexgame.model.TileType;

public class PlayerDTO {
    
    public int playerIndex;
    public String userId;
    public String name;
    public String color;
    public HashMap<TileType, Integer> tradeFactor;

    public int roadNumber;
    public int villageNumber;
    public int cityNumber;

    public int numberDevelopments;
    public ArrayDeque<DevelopmentItem> usedDevelopments;
    public int playedKnights;

    public int victoryPoints;

    public int totalResBalance;
    public int resDebt;

    public boolean canBuyRoad;
    public boolean canBuyVillage;
    public boolean canBuyCity;
    public boolean canBuyDevelopment;



    public HashMap<TileType, Integer> resBalance;
    public ArrayDeque<DevelopmentItem> developments;

    public PlayerDTO(Player p, boolean isPlacementPhase) {
        this.playerIndex = p.getPlayerIndex();
        this.userId = p.getUserId();
        this.name = p.getName();
        this.color = p.getColor();
        this.tradeFactor = p.getTradeFactor();

        this.roadNumber = p.getRoadNumber();
        this.villageNumber = p.getVillageNumber();
        this.cityNumber = p.getCityNumber();

        this.numberDevelopments = p.getNumberDevelopments();
        this.usedDevelopments = p.getUsedDevelopments();
        this.playedKnights = p.getPlayedKnights();

        this.victoryPoints = p.getVictoryPoints();

        this.totalResBalance = p.getTotalResBalance();
        this.resDebt = p.getResDebt();


        this.canBuyRoad = p.canBuildFreeRoad(isPlacementPhase) || p.canBuildRoad();
        this.canBuyVillage = (p.canBuildFreeVillage() && isPlacementPhase) || p.canBuildVillage();
        this.canBuyCity = p.canBuildCity();
        this.canBuyDevelopment = p.canBuyDevelopment();


        this.resBalance = p.getResBalance();
        this.developments = p.getDevelopments();
    }
}
