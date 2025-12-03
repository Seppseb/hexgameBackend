package com.example.hexgame.model;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Random;


public class Bank {
    
    private HashMap<TileType, Integer> resBalance;

    private ArrayDeque<DevelopmentItem> developments;

    private Random random;

    public Bank(Random random) {

        this.random = random;

        this.resBalance = new HashMap<TileType, Integer>();
        resBalance.put(TileType.wood, 19);
        resBalance.put(TileType.clay, 19);
        resBalance.put(TileType.wheat, 19);
        resBalance.put(TileType.wool, 19);
        resBalance.put(TileType.stone, 19);

        this.developments = new ArrayDeque<DevelopmentItem>();

        int knight = 14;
        int development = 2;
        int roadwork = 2;
        int monopoly = 2;
        int victoryPoint = 5;

        for (int totalCards = knight + victoryPoint + development + roadwork + monopoly; totalCards > 0; totalCards--) {
            // 0 - totalCards-1; -> +1 -> 1 - totalCards
            int cardNumber = random.nextInt(totalCards) + 1;

            int limit = knight;
            if (cardNumber <= limit) {
                if (knight < 1) throw new java.lang.Error("BUG: took card that doesnt exist");
                knight--;
                this.developments.add(new DevelopmentItem(DevelopmentType.knight));
                continue;
            }
            limit+= victoryPoint;
            if (cardNumber <= limit) {
                if (victoryPoint < 1) throw new java.lang.Error("BUG: took card that doesnt exist");
                victoryPoint--;
                this.developments.add(new DevelopmentItem(DevelopmentType.victoryPoint));
                continue;
            }
            limit+= development;
            if (cardNumber <= limit) {
                if (development < 1) throw new java.lang.Error("BUG: took card that doesnt exist");
                development--;
                this.developments.add(new DevelopmentItem(DevelopmentType.development));
                continue;
            }
            limit+= roadwork;
            if (cardNumber <= limit) {
                if (roadwork < 1) throw new java.lang.Error("BUG: took card that doesnt exist");
                roadwork--;
                this.developments.add(new DevelopmentItem(DevelopmentType.roadwork));
                continue;
            }
            if (monopoly < 1) throw new java.lang.Error("BUG: took card that doesnt exist");
            monopoly--;
            this.developments.add(new DevelopmentItem(DevelopmentType.monopoly));
        }
    }

    public boolean hasRes(TileType type, int amount) {
        if (resBalance.get(type) == null) return false;
        return resBalance.get(type) >= amount;
    }

    public int takeRes(TileType type, int amount) {
        if (resBalance.get(type) == null) return 0;
        int avaliableAmount = resBalance.get(type);
        if (avaliableAmount <= 0) return 0;
        int actualAmount = amount <= avaliableAmount ? amount : avaliableAmount;
        resBalance.put(type, resBalance.get(type) - actualAmount);
        return actualAmount;
    }

    public void addRes(TileType type, int amount) {
        if (resBalance.get(type) == null) return;
        resBalance.put(type, resBalance.get(type) + amount);
    }

    public HashMap<TileType, Integer> getResBalance() {
        return resBalance;
    }

    public ArrayDeque<DevelopmentItem> getDevelopments() {
        return developments;
    }

}
