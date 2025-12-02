package com.example.hexgame.model;

import java.util.HashMap;
import java.util.Random;


public class Bank {
    
    private HashMap<TileType, Integer> resBalance; 

    private int knight;
    private int victoryPoint;
    private int development;
    private int roadwork;
    private int monopoly;

    private Random random;


    public Bank(Random random) {
        this.resBalance = new HashMap<TileType, Integer>();
        resBalance.put(TileType.wood, 19);
        resBalance.put(TileType.clay, 19);
        resBalance.put(TileType.wheat, 19);
        resBalance.put(TileType.wool, 19);
        resBalance.put(TileType.stone, 19);

        this.knight = 14;
        this.development = 2;
        this.roadwork = 2;
        this.monopoly = 2;
        this.victoryPoint = 5;

        this.random = random;
    }

    public boolean hasRes(TileType type, int amount) {
        return resBalance.get(type) >= amount;
    }

    public int takeRes(TileType type, int amount) {
        int avaliableAmount = resBalance.get(type);
        if (avaliableAmount <= 0) return 0;
        int actualAmount = amount <= avaliableAmount ? amount : avaliableAmount;
        resBalance.put(type, resBalance.get(type) - actualAmount);
        return actualAmount;
    }

    public void addRes(TileType type, int amount) {
        resBalance.put(type, resBalance.get(type) + amount);
    }

    public String drawDevelopmentCard() {
        int totalCards = knight + victoryPoint + development + roadwork + monopoly;
        if (totalCards == 0) return "";
        // 0 - totalCards;
        int cardNumber = random.nextInt(totalCards) + 1;
        int limit = knight;
        if (cardNumber <= limit) {
            if (knight < 1) throw new java.lang.Error("BUG: took card that doesnt exist");
            knight--;
            return "knight";
        }
        limit+= victoryPoint;
        if (cardNumber <= limit) {
            if (victoryPoint < 1) throw new java.lang.Error("BUG: took card that doesnt exist");
            victoryPoint--;
            return "victoryPoint";
        }
        limit+= development;
        if (cardNumber <= limit) {
            if (development < 1) throw new java.lang.Error("BUG: took card that doesnt exist");
            development--;
            return "development";
        }
        limit+= roadwork;
        if (cardNumber <= limit) {
            if (roadwork < 1) throw new java.lang.Error("BUG: took card that doesnt exist");
            roadwork--;
            return "roadwork";
        }
        if (monopoly < 1) throw new java.lang.Error("BUG: took card that doesnt exist");
        monopoly--;
        return "monopoly";
    }

    public int getKnight() {
        return knight;
    }

    public int getVictoryPoint() {
        return victoryPoint;
    }

    public int getDevelopment() {
        return development;
    }

    public int getRoadwork() {
        return roadwork;
    }

    public int getMonopoly() {
        return monopoly;
    }

    public HashMap<TileType, Integer> getResBalance() {
        return resBalance;
    }

}
