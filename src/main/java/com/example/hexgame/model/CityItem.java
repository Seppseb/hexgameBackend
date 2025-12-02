package com.example.hexgame.model;

import java.util.HashMap;

public class CityItem extends ShopItem {
    public CityItem() {
        this.cost = new HashMap<TileType, Integer>();
        this.cost.put(TileType.stone, 3);
        this.cost.put(TileType.wheat, 2);
    }
}
