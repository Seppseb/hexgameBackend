package com.example.hexgame.model;

public class Port {
    
    private TileType type;

    private boolean waterLeftSide;

    public Port(TileType type, boolean waterLeftSide) {
        this.type = type;
        this.waterLeftSide = waterLeftSide;
    }

    public TileType getType() {
        return type;
    }

    public boolean getWaterLeftSide() {
        return this.waterLeftSide;
    }

}
