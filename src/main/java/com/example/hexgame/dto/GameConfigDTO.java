package com.example.hexgame.dto;

public class GameConfigDTO {

    private static final int DEFAULT_NUMBER_ORDER = 0; // FAIREST_FIXED
    //private static final boolean DEFAULT_SHOW_BANK = false;
    private static final int DEFAULT_BOARD_SIZE = 5;
    private static final int DEFAULT_VILLAGE_NUMBER = 5;
    private static final int DEFAULT_CITY_NUMBER = 4;
    //private static final int DEFAULT_ROAD_NUMBER = 15;
    //private static final int DEFAULT_NEEDED_VPS = 10;


    private static final int MIN_NUMBER_ORDER = 0;
    private static final int MAX_NUMBER_ORDER = 2;

    private static final int MIN_BOARD_SIZE = 3;
    private static final int MAX_BOARD_SIZE = 11;

    private static final int MIN_VILLAGE_NUMBER = 2;
    private static final int MAX_VILLAGE_NUMBER = 999;

    private static final int MIN_CITY_NUMBER = 0;
    private static final int MAX_CITY_NUMBER = 999;

    private static final int MAX_ROADS = 9999;

    private static final int MIN_NEEDED_VPS = 3;


    private int numberOrder;
    private boolean showBank;
    private int boardSize;
    private int villageNumber;
    private int cityNumber;
    private int roadNumber;
    private int neededVictoryPoints;

    public void makeValid() {

        if (numberOrder < MIN_NUMBER_ORDER || numberOrder > MAX_NUMBER_ORDER) {
            numberOrder = DEFAULT_NUMBER_ORDER;
        }
    
        if (boardSize < MIN_BOARD_SIZE || boardSize > MAX_BOARD_SIZE) {
            boardSize = DEFAULT_BOARD_SIZE;
        }
        if (boardSize % 2 == 0) {
            boardSize -= 1;
        }
    
        if (villageNumber < MIN_VILLAGE_NUMBER || villageNumber > MAX_VILLAGE_NUMBER) {
            villageNumber = DEFAULT_VILLAGE_NUMBER;
        }
    
        if (cityNumber < MIN_CITY_NUMBER || cityNumber > MAX_CITY_NUMBER) {
            cityNumber = DEFAULT_CITY_NUMBER;
        }
    
        int minRoads = Math.max((cityNumber + villageNumber) * 2 - 3, 5);
        if (roadNumber < minRoads || roadNumber > MAX_ROADS) {
            roadNumber = minRoads;
        }
    
        int maxVps = cityNumber * 2 + villageNumber + 2;
        if (neededVictoryPoints < MIN_NEEDED_VPS || neededVictoryPoints > maxVps) {
            neededVictoryPoints = maxVps;
        }
    }
    

    public int getNumberOrder() {
        return numberOrder;
    }

    public void setNumberOrder(int numberOrder) {
        this.numberOrder = numberOrder;
    }

    public boolean getShowBank() {
        return showBank;
    }

    public void setShowBank(boolean showBank) {
        this.showBank = showBank;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public int getVillageNumber() {
        return villageNumber;
    }

    public void setVillageNumber(int villageNumber) {
        this.villageNumber = villageNumber;
    }

    public int getCityNumber() {
        return cityNumber;
    }

    public void setCityNumber(int cityNumber) {
        this.cityNumber = cityNumber;
    }

    public int getRoadNumber() {
        return roadNumber;
    }

    public void setRoadNumber(int roadNumber) {
        this.roadNumber = roadNumber;
    }

    public int getNeededVictoryPoints() {
        return neededVictoryPoints;
    }

    public void setNeededVictoryPoints(int neededVictoryPoints) {
        this.neededVictoryPoints = neededVictoryPoints;
    }
    
}
