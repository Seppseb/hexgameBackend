package com.example.hexgame.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Player {
    private String userId; // set by server (UUID string)
    private String name;   // optional display name
    private int playerIndex;
    private Player nextPlayer;
    private String color;
    private Bank bank;

    private int wood;
    private int clay;
    private int wool;
    private int wheat;
    private int stone;

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

    public boolean canBuildRoad() {
        return wood >= 1 && clay >= 1 && road >= 1;
    }

    public boolean buildRoad() {
        if (!canBuildRoad()) return false;
        wood-=1;
        clay-=1;
        bank.addWood(1);
        bank.addClay(1);

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
        return wood >= 1 && clay >= 1 && wheat >= 1 && wool >= 1 && village >= 1;
    }

    public boolean buildVillage() {
        if (!canBuildVillage()) return false;
        wood-=1;
        clay-=1;
        wheat-=1;
        wool-=1;
        bank.addWood(1);
        bank.addClay(1);
        bank.addWheat(1);
        bank.addWool(1);

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
        return wheat >= 2 && stone >= 3 && city >= 1;
    }

    public boolean buildCity() {
        if (!canBuildCity()) return false;
        wheat-=2;
        stone-=3;
        bank.addWheat(2);
        bank.addStone(3);

        city--;
        village++;
        return true;
    }

    public int drawWood(int wood) {
        int amout = bank.takeWood(wood);
        this.wood += amout;
        return amout;
    }

    public int drawClay(int clay) {
        int amout = bank.takeClay(clay);
        this.clay += amout;
        return amout;
    }

    public int drawWheat(int wheat) {
        int amout = bank.takeWheat(wheat);
        this.wheat += amout;
        return amout;
    }

    public int drawWool(int wool) {
        int amout = bank.takeWool(wool);
        this.wool += amout;
        return amout;
    }

    public int drawStone(int stone) {
        int amout = bank.takeStone(stone);
        this.stone += amout;
        return amout;
    }

    public int drawType(TileType type, int amout) {
        switch (type) {
            case wood:
                return drawWood(amout);
            case clay:
                return drawClay(amout);
            case wheat:
                return drawWheat(amout);
            case wool:
                return drawWool(amout);
            case stone:
                return drawStone(amout);
            case desert:
                return 0;
            default: throw new java.lang.Error("BUG: bad ressource type");
        }
    }
    public int getWood() {
        return wood;
    }
    public int getClay() {
        return clay;
    }
    public int getWool() {
        return wool;
    }
    public int getWheat() {
        return wheat;
    }
    public int getStone() {
        return stone;
    }

    
}
