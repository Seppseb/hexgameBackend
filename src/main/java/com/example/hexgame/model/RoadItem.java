package com.example.hexgame.model;

import java.util.HashMap;

public class RoadItem extends ShopItem {
    public RoadItem() {
        this.cost = new HashMap<TileType, Integer>();
        this.cost.put(TileType.wood, 1);
        this.cost.put(TileType.clay, 1);
    }
}
