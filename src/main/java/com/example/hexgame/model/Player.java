package com.example.hexgame.model;

import java.util.ArrayDeque;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Player {
    private String userId; // set by server (UUID string)
    private String name;   // optional display name
    private int playerIndex;
    private Player nextPlayer;
    private String color;
    private int victoryPoints;
    
    private Bank bank;
    private GameInstance game;

    private HashMap<TileType, Integer> resBalance; 

    private HashMap<TileType, Integer> tradeFactor; 

    private int resDebt;
    private int freeRoads;

    private int playedKnights;

    private ArrayDeque<DevelopmentItem> developments;
    private ArrayDeque<DevelopmentItem> usedDevelopments;


    private ArrayDeque<RoadItem> roads;
    private ArrayDeque<VillageItem> villages;
    private ArrayDeque<CityItem> cities;

    //TODO make variable, also other stuff like road amount and other things
    private int pointsToWin = 10;

    public void setNextPlayer(Player nextPlayer) {
        this.nextPlayer = nextPlayer;
    }

    
    public void setPlayerIndex(int playerIndex) {
        this.playerIndex = playerIndex;
    }


    public Player(String userId, String name, Bank bank, GameInstance gameInstance) {
        this.userId = userId;
        this.name = name;
        this.bank = bank;
        this.game = gameInstance;
        this.victoryPoints = 0;
        this.resDebt = 0;
        this.freeRoads = 0;
        this.playedKnights = 0;

        this.roads = new ArrayDeque<RoadItem>();
        for (int i = 0; i < 15; i++) {
            this.roads.add(new RoadItem());
        }
        this.villages = new ArrayDeque<VillageItem>();
        for (int i = 0; i < 5; i++) {
            this.villages.add(new VillageItem());
        }
        this.cities = new ArrayDeque<CityItem>();
        for (int i = 0; i < 4; i++) {
            this.cities.add(new CityItem());
        }

        this.developments = new ArrayDeque<DevelopmentItem>();
        this.usedDevelopments = new ArrayDeque<DevelopmentItem>();


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

    @JsonIgnore
    public Player getNextPlayer() {
        return nextPlayer;
    }
    
    public int getPlayerIndex() {
        return playerIndex;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    //private
    public HashMap<TileType, Integer> getResBalance() {
        return resBalance;
    }

    public int getTotalResBalance() {
        int total = 0;
        for (int amount: this.resBalance.values()) {
            total += amount;
        }
        return total;
    }

    public HashMap<TileType, Integer> getTradeFactor() {
        return tradeFactor;
    }

    public int getTradeFactor(TileType res) {
        return tradeFactor.get(res);
    }

    public int getRoadNumber() {
        return roads.size();
    }

    public int getVillageNumber() {
        return villages.size();
    }

    public int getCityNumber() {
        return cities.size();
    }

    //private
    public ArrayDeque<DevelopmentItem> getDevelopments() {
        return developments;
    }

    public int getNumberDevelopments() {
        return developments == null ? 0 : developments.size();
    }

    public ArrayDeque<DevelopmentItem> getUsedDevelopments() {
        return usedDevelopments;
    }

    public int getPlayedKnights() {
        return playedKnights;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }

    public int getResDebt() {
        return resDebt;
    }

    public DevelopmentItem getDevelopmentCard(String type) {
        DevelopmentType cardType;
        switch (type) {
            case "knight":
                cardType = DevelopmentType.knight;
                break;
            case "development":
                cardType = DevelopmentType.development;
                break;
            case "roadwork":
                cardType = DevelopmentType.roadwork;
                break;
            case "monopoly":
                cardType = DevelopmentType.monopoly;
                break;
            case "victoryPoint":
                cardType = DevelopmentType.victoryPoint;
                break;
            default:
                return null;
        }

        for (DevelopmentItem card: developments) {
            if (card.getType() == cardType) return card;
        }

        return null;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String toString() {
        return name;      
    }

    public boolean hasRes(TileType type, int amount) {
        if (resBalance.get(type) == null) return false;
        return resBalance.get(type) >= amount;
    }

    public boolean takeRes(TileType type, int amount) {
        if (!hasRes(type, amount)) return false;
        if (resBalance.get(type) == null) return false;
        resBalance.put(type, resBalance.get(type) - amount);
        bank.addRes(type, amount);
        return true;
    }

    public int addRes(TileType type, int amount) {
        if (resBalance.get(type) == null) return 0;
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
        return this.roads.size() >= 1 && this.canBuildItem(this.roads.peekFirst());
    }

    public boolean buildRoad() {
        if (!canBuildRoad()) return false;
        this.buildItem(this.roads.removeFirst());
        return true;
    }

    public boolean canBuildFreeRoad(boolean initial) {
        if (!initial && freeRoads < 1) return false;
        return this.roads.size() >= 1;
    }

    public boolean buildFreeRoad(boolean initial) {
        if (!canBuildFreeRoad(initial)) return false;
        if (!initial) freeRoads--;
        this.roads.removeFirst();
        return true;
    }

    public boolean canBuildVillage() {
        return this.villages.size() >= 1 && canBuildItem(this.villages.peekFirst());
    }

    public boolean buildVillage() {
        if (!canBuildVillage()) return false;
        this.buildItem(this.villages.removeFirst());
        addVictoryPoints(1);
        return true;
    }

    public boolean canBuildFreeVillage() {
        return this.villages.size() >= 1;
    }

    public boolean buildFreeVillage() {
        if (!canBuildFreeVillage()) return false;
        this.villages.removeFirst();
        addVictoryPoints(1);
        return true;
    }

    public boolean canBuildCity() {
        return this.cities.size() >= 1 && canBuildItem(this.cities.peekFirst());
    }

    public boolean buildCity() {
        if (!canBuildCity()) return false;
        buildItem(this.cities.removeFirst());
        this.villages.add(new VillageItem());
        addVictoryPoints(1);
        return true;
    }

    public boolean canBuyDevelopment() {
        return this.bank.getDevelopments().size() >= 1 && canBuildItem(this.bank.getDevelopments().peekFirst());
    }

    public boolean buyDevelopment() {
        if (!canBuyDevelopment()) return false;
        DevelopmentItem development = this.bank.getDevelopments().removeFirst();
        buildItem(development);
        this.developments.add(development);
        return true;
    }

    public void buildPort(TileType res) {
        if (res != null) {
            switch (res) {
                case wood:
                    this.tradeFactor.put(TileType.wood, 2);
                    return;
                case clay:
                    this.tradeFactor.put(TileType.clay, 2);
                    return;
                case wheat:
                    this.tradeFactor.put(TileType.wheat, 2);
                    return;
                case wool:
                    this.tradeFactor.put(TileType.wool, 2);
                    return;
                case stone:
                    this.tradeFactor.put(TileType.stone, 2);
                    return;
                default:
                    break;
            }
        }
        for (TileType type: this.tradeFactor.keySet()) {
            if (tradeFactor.get(type) > 3) {
                tradeFactor.put(type, 3);
            }
        }

    }

    public void addVictoryPoints(int points) {
        victoryPoints += points;
        if (victoryPoints >= pointsToWin) {
            game.sendWin(this);
        }
    }

    public void playDevelopmentCard(DevelopmentItem card) {
        developments.remove(card);
        usedDevelopments.add(card);
        if (card.getType() == DevelopmentType.knight) playedKnights++;
    }

    public void addToResDebt(int amount) {
        this.resDebt+= amount;
    }

    public void addFreeRoads(int amount) {
        int roadsLeft = this.roads.size();
        if (roadsLeft < amount) {
            amount = roadsLeft;
        }
        this.freeRoads+= amount;
    }

    public void stealRandomRessource(Player profiteur) {
        int totalRes = this.getTotalResBalance();
        if (totalRes == 0) return;
        // 0 - totalRes;
        int resNumber = this.game.getRandom().nextInt(totalRes) + 1;
        int limit = 0;
        for (TileType type: resBalance.keySet()) {
            limit+= resBalance.get(type);
            if (resNumber <= limit) {
                if (resBalance.get(type) < 1) throw new java.lang.Error("BUG: took ressource from player that doesnt exist");
                this.takeRes(type, 1);
                profiteur.addRes(type, 1);
                return;
            }
        }
        throw new java.lang.Error("BUG: cant steal from player");
    }

}
