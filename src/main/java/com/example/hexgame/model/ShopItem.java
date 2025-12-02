package com.example.hexgame.model;

import java.util.HashMap;

public abstract class ShopItem {
    protected HashMap<TileType, Integer> cost;


    public HashMap<TileType, Integer> getCost() {
        return cost;
    }
}

