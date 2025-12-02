package com.example.hexgame.model;

import java.util.HashMap;

public class VillageItem extends ShopItem {
    public VillageItem() {
        this.cost = new HashMap<TileType, Integer>();
        this.cost.put(TileType.wood, 1);
        this.cost.put(TileType.clay, 1);
        this.cost.put(TileType.wheat, 1);
        this.cost.put(TileType.wool, 1);
    }
}
