package com.example.hexgame.model;

import java.util.Random;


public class Bank {
    
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

    private Random random;


    public Bank(Random random) {
        this.wood = 19;
        this.clay = 19;
        this.wool = 19;
        this.wheat = 19;
        this.stone = 19;

        this.knight = 14;
        this.development = 2;
        this.roadwork = 2;
        this.monopoly = 2;
        this.victoryPoint = 5;

        this.random = random;
    }

    public void addWood(int wood) {
        this.wood += wood;
    }
    public void addClay(int clay) {
        this.clay += clay;
    }
    public void addWool(int wool) {
        this.wool += wool;
    }
    public void addWheat(int wheat) {
        this.wheat += wheat;
    }
    public void addStone(int stone) {
        this.stone += stone;
    }

    public int takeWood(int wood) {
        if (wood <= 0) return 0;
        int avaliable = wood <= this.wood ? wood : this.wood;
        this.wood-=avaliable;
        if (avaliable < 0 || this.wood < 0) throw new java.lang.Error("BUG: bank has negative amount");
        return avaliable;
    }
    public int takeClay(int clay) {
        if (clay <= 0) return 0;
        int avaliable = clay <= this.clay ? clay : this.clay;
        this.clay-=avaliable;
        if (avaliable < 0 || this.clay < 0) throw new java.lang.Error("BUG: bank has negative amount");
        return avaliable;
    }
    public int takeWool(int wool) {
        if (wool <= 0) return 0;
        int avaliable = wool <= this.wool ? wool : this.wool;
        this.wool-=avaliable;
        if (avaliable < 0 || this.wool < 0) throw new java.lang.Error("BUG: bank has negative amount");
        return avaliable;
    }
    public int takeWheat(int wheat) {
        if (wheat <= 0) return 0;
        int avaliable = wheat <= this.wheat ? wheat : this.wheat;
        this.wheat-=avaliable;
        if (avaliable < 0 || this.wheat < 0) throw new java.lang.Error("BUG: bank has negative amount");
        return avaliable;
    }
    public int takeStone(int stone) {
        if (stone <= 0) return 0;
        int avaliable = stone <= this.stone ? stone : this.stone;
        this.stone-=avaliable;
        if (avaliable < 0 || this.stone < 0) throw new java.lang.Error("BUG: bank has negative amount");
        return avaliable;
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

}
