package com.example.hexgame.model;

import java.util.Arrays;
import java.util.Map;

public enum BoardConfigs {
//TODO set other values-> 3, 7, 9, 11
//TODO handle other sizes frontend
    SIZE_3(new BoardConfig(
        3,
        Map.of(
            TileType.wood, 4,
            TileType.clay, 3,
            TileType.wool, 4,
            TileType.wheat, 4,
            TileType.stone, 3,
            TileType.desert, 1
        ),
        Map.of(
            TileType.wood, 1,
            TileType.clay, 1,
            TileType.wool, 1,
            TileType.wheat, 1,
            TileType.stone, 1,
            TileType.desert, 4
        ),
        new int[] {0, 0, 1, 2, 2, 2, 2, 0, 2, 2, 2, 2, 1},
        new int [] {5, 2, 6, 3, 8, 10, 9, 12, 11, 4, 8, 10, 9, 4, 5, 6, 3, 11}
    )),

    SIZE_5(new BoardConfig(
        5,
        Map.of(
            TileType.wood, 4,
            TileType.clay, 3,
            TileType.wool, 4,
            TileType.wheat, 4,
            TileType.stone, 3,
            TileType.desert, 1
        ),
        Map.of(
            TileType.wood, 1,
            TileType.clay, 1,
            TileType.wool, 1,
            TileType.wheat, 1,
            TileType.stone, 1,
            TileType.desert, 4
        ),
        new int[] {0, 0, 1, 2, 2, 2, 2, 0, 2, 2, 2, 2, 1},
        new int [] {5, 2, 6, 3, 8, 10, 9, 12, 11, 4, 8, 10, 9, 4, 5, 6, 3, 11}
    )),

    SIZE_7(new BoardConfig(
            7,
            Map.of(
                TileType.wood, 40,
                TileType.clay, 3,
                TileType.wool, 4,
                TileType.wheat, 4,
                TileType.stone, 3,
                TileType.desert, 1
            ),
            Map.of(
                TileType.wood, 1,
                TileType.clay, 1,
                TileType.wool, 1,
                TileType.wheat, 1,
                TileType.stone, 1,
                TileType.desert, 4
            ),
            new int[] {0, 0, 1, 2, 20, 20, 2, 0, 2, 2, 2, 2, 1},
            new int [] {5, 2, 6, 3, 8, 10, 9, 12, 11, 4, 8, 10, 9, 4, 5, 6, 3, 11}
    ));

    private final BoardConfig config;

    BoardConfigs(BoardConfig config) {
        this.config = config;
    }

    public BoardConfig getConfig() {
        return config;
    }

    public static BoardConfig fromSize(int size) {
        return Arrays.stream(values())
                .map(BoardConfigs::getConfig)
                .filter(c -> c.getSize() == size)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported size"));
    }
}

