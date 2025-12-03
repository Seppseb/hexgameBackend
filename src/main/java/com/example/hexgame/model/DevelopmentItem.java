package com.example.hexgame.model;

import java.util.HashMap;

public class DevelopmentItem extends ShopItem {

    private DevelopmentType type;

    public DevelopmentItem(DevelopmentType type) {
        this.type = type;

        this.cost = new HashMap<TileType, Integer>();
        this.cost.put(TileType.wheat, 1);
        this.cost.put(TileType.wool, 1);
        this.cost.put(TileType.stone, 1);
    }

    public DevelopmentType getType() {
        return type;
    }

}