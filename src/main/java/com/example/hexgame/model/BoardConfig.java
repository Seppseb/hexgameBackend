package com.example.hexgame.model;

import java.util.Map;

public class BoardConfig {

    private final int size;
    private final Map<TileType, Integer> tileCounts;
    private final Map<TileType, Integer> portCounts;
    private final int[] numberQuantity;
    private final int[] defaultNumberOrder;


    public BoardConfig(int size, Map<TileType, Integer> tileCounts, Map<TileType, Integer> portCounts, int[] numberQuantity, int[] defaultNumberOrder) {
        this.size = size;
        this.tileCounts = tileCounts;
        this.portCounts = portCounts;
        this.numberQuantity = numberQuantity;
        this.defaultNumberOrder = defaultNumberOrder;
    }

    public int getSize() { return size; }
    public Map<TileType, Integer> getTileCounts() { return tileCounts; }
    public Map<TileType, Integer> getPortCounts() { return portCounts; }
    public int[] getNumberQuantity() { return numberQuantity; }
    public int[] getDefaultNumberOrder() { return defaultNumberOrder; }
}

