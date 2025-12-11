package com.example.hexgame.model;

public class MostKnightsCard {

    private Player owner;
    private int currentLimit = 2;

    public MostKnightsCard() {
        this.owner = null;
    }

    public void checkOwnerChange(Player newOwner) {
        int amount = newOwner.getPlayedKnights();
        if (amount <= currentLimit) return;
        currentLimit = amount;
        if (owner == newOwner) return;
        if (owner != null) {
            owner.addVictoryPoints(-2);
        }
        owner = newOwner;
        owner.addVictoryPoints(2);
    }

}
