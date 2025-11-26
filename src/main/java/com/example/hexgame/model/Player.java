package com.example.hexgame.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Player {
    private String userId; // set by server (UUID string)
    private String name;   // optional display name
    private int initialDice;
    private Player nextPlayer;
    private String color;

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
    public int getInitialDice() {
        return initialDice;
    }
    public void setInitialDice(int initialDice) {
        this.initialDice = initialDice;
    }
    // constructors, getters, setters
    public Player() {}
    public Player(String userId, String name) {
        this.userId = userId; this.name = name;
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
        wood--;
        clay--;

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
        wood--;
        clay--;
        wheat--;
        wool--;

        village--;
        return true;
    }

    public boolean canBuildFreeVillage() {
        return village < 1;
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
        wheat--;
        wheat--;
        stone--;
        stone--;
        stone--;

        city--;
        return true;
    }
}
