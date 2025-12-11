package com.example.hexgame.model;

import java.util.HashMap;

public class TradeOffer {
    private String offererId;
    private int wood;
    private int clay;
    private int wheat;
    private int wool;
    private int stone;

    private HashMap<String, Boolean> accepterIds;

    public TradeOffer(String offererId, int wood, int clay, int wheat, int wool, int stone) {
        this.offererId = offererId;
        this.wood = wood;
        this.clay = clay;
        this.wheat = wheat;
        this.wool = wool;
        this.stone = stone;
        this.accepterIds = new  HashMap<String, Boolean>();
    }

    public String getOffererId() {
        return offererId;
    }

    public int getWood() {
        return wood;
    }

    public int getClay() {
        return clay;
    }

    public int getWheat() {
        return wheat;
    }

    public int getWool() {
        return wool;
    }

    public int getStone() {
        return stone;
    }

    public  HashMap<String, Boolean> getAcceptersId() {
        return accepterIds;
    }

    public void accept(String accepterId) {
        this.accepterIds.put(accepterId, true);
    }

    public void decline(String accepterId) {
        this.accepterIds.put(accepterId, false);
    }

    public boolean hasValues(int wood, int clay, int wheat, int wool, int stone) {
        if (this.wood != wood) return false;
        if (this.clay != clay) return false;
        if (this.wheat != wheat) return false;
        if (this.wool != wool) return false;
        if (this.stone != stone) return false;
        return true;
    }

}
