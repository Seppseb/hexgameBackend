package com.example.hexgame.model;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Player {
    private String userId; // set by server (UUID string)
    private String name;   // optional display name
    private int playerIndex;
    private Player nextPlayer;
    private String color;
    private Bank bank;

    private HashMap<TileType, Integer> resBalance; 

    private HashMap<TileType, Integer> tradeFactor; 

    private int knight;
    private int victoryPoint;
    private int development;
    private int roadwork;
    private int monopoly;

    private int road;
    private int village;
    private int city;

    @JsonIgnore
    public Player getNextPlayer() {
        return nextPlayer;
    }
    public void setNextPlayer(Player nextPlayer) {
        this.nextPlayer = nextPlayer;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }
    public void setPlayerIndex(int playerIndex) {
        this.playerIndex = playerIndex;
    }
    // constructors, getters, setters
    public Player(String userId, String name, Bank bank) {
        this.userId = userId;
        this.name = name;
        this.bank = bank;
        this.road = 15;
        this.village = 5;
        this.city = 4;
        this.tradeFactor = new HashMap<TileType, Integer>();
        tradeFactor.put(TileType.wood, 4);
        tradeFactor.put(TileType.clay, 4);
        tradeFactor.put(TileType.wheat, 4);
        tradeFactor.put(TileType.wool, 4);
        tradeFactor.put(TileType.stone, 4);
        this.resBalance = new HashMap<TileType, Integer>();
        resBalance.put(TileType.wood, 0);
        resBalance.put(TileType.clay, 0);
        resBalance.put(TileType.wheat, 0);
        resBalance.put(TileType.wool, 0);
        resBalance.put(TileType.stone, 0);
    }
    // getters/setters...
    public String getUserId() {
        return userId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String toString() {
        return name;      
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean hasRes(TileType type, int amount) {
        return resBalance.get(type) >= amount;
    }

    public boolean takeRes(TileType type, int amount) {
        if (!hasRes(type, amount)) return false;
        resBalance.put(type, resBalance.get(type) - amount);
        bank.addRes(type, amount);
        return true;
    }

    public int addRes(TileType type, int amount) {
        int actualAmount = bank.takeRes(type, amount);
        resBalance.put(type, resBalance.get(type) + actualAmount);
        return actualAmount;
    }

    public boolean canBuildItem(ShopItem item) {
        for (TileType type: item.getCost().keySet()) {
            int resCost = item.getCost().get(type);
            if (!this.hasRes(type, resCost)) return false;
        }
        return true;
    }

    public boolean buildItem(ShopItem item) {
        if (!canBuildItem(item)) return false;
        for (TileType type: item.getCost().keySet()) {
            int resCost = item.getCost().get(type);
            this.takeRes(type, resCost);
        }
        return true;
    }

    public boolean canBuildRoad() {
        return this.canBuildItem(new RoadItem()) && road >= 1;
    }

    public boolean buildRoad() {
        if (!canBuildRoad()) return false;
        this.buildItem(new RoadItem());
        road--;
        return true;
    }

    public boolean canBuildFreeRoad() {
        return road >= 1;
    }

    public boolean buildFreeRoad() {
        if (!canBuildFreeRoad()) return false;
        road--;
        return true;
    }

    public boolean canBuildVillage() {
        return canBuildItem(new VillageItem()) && village >= 1;
    }

    public boolean buildVillage() {
        if (!canBuildVillage()) return false;
        this.buildItem(new VillageItem());

        village--;
        return true;
    }

    public boolean canBuildFreeVillage() {
        return village >= 1;
    }

    public boolean buildFreeVillage() {
        if (!canBuildFreeVillage()) return false;
        village--;
        return true;
    }

    public boolean canBuildCity() {
        return canBuildItem(new CityItem()) && city >= 1;
    }

    public boolean buildCity() {
        if (!canBuildCity()) return false;
        buildItem(new CityItem());

        city--;
        village++;
        return true;
    }


    public HashMap<TileType, Integer> getResBalance() {
        return resBalance;
    }

    public HashMap<TileType, Integer> getTradeFactor() {
        return tradeFactor;
    }

    public int getTradeFactor(TileType res) {
        return tradeFactor.get(res);
    }

    public void buildPort(TileType res) {
        switch (res) {
                case wood:
                    this.tradeFactor.put(TileType.wood, 2);
                    break;
                case clay:
                    this.tradeFactor.put(TileType.clay, 2);
                    break;
                case wheat:
                    this.tradeFactor.put(TileType.wheat, 2);
                    break;
                case wool:
                    this.tradeFactor.put(TileType.wool, 2);
                    break;
                case stone:
                    this.tradeFactor.put(TileType.stone, 2);
                    break;
                default:
                    for (TileType type: this.tradeFactor.keySet()) {
                        if (tradeFactor.get(type) > 3) {
                            tradeFactor.put(type, 3);
                        }
                    }
                    break;
        }
    }

    
}
