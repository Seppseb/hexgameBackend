package com.example.hexgame.model;

import java.util.Arrays;
import java.util.Map;

public enum BoardConfigs {
    SIZE_3(new BoardConfig(
        3,
        Map.of(
            TileType.wood, 1,
            TileType.clay, 1,
            TileType.wool, 2,
            TileType.wheat, 2,
            TileType.stone, 1,
            TileType.desert, 0
        ),
        Map.of(
            TileType.wood, 1,
            TileType.clay, 1,
            TileType.wool, 1,
            TileType.wheat, 1,
            TileType.stone, 1,
            TileType.desert, 4
        ),
        new int[] {0, 0, 0, 0, 1, 1, 2, 0, 1, 1, 1, 0, 0},
        new int [] {6, 10, 8, 4, 6, 9, 5}
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
            TileType.wood, 8,
            TileType.clay, 6,
            TileType.wool, 8,
            TileType.wheat, 7,
            TileType.stone, 6,
            TileType.desert, 2
        ),
        Map.of(
            TileType.wood, 1,
            TileType.clay, 1,
            TileType.wool, 1,
            TileType.wheat, 1,
            TileType.stone, 1,
            TileType.desert, 4
        ),
        new int[] {0, 0, 2, 4, 4, 4, 4, 0, 4, 4, 3, 4, 2},
        new int [] {5, 2, 6, 3, 8, 10, 9, 12, 11, 4, 8, 10, 9, 4, 5, 6, 3, 11, 5, 2, 4, 3, 8, 10, 9, 12, 11, 4, 8, 11, 9, 6, 5, 6, 3}
    )),

    SIZE_9(new BoardConfig(
        9,
        Map.of(
            TileType.wood, 13,
            TileType.clay, 10,
            TileType.wool, 13,
            TileType.wheat, 13,
            TileType.stone, 9,
            TileType.desert, 3
        ),
        Map.of(
            TileType.wood, 1,
            TileType.clay, 1,
            TileType.wool, 1,
            TileType.wheat, 1,
            TileType.stone, 1,
            TileType.desert, 4
        ),
        new int[] {0, 0, 3, 6, 7, 7, 6, 0, 6, 7, 7, 6, 3},
        new int [] {5, 2, 6, 3, 8, 10, 9, 12, 11, 4, 8, 10, 9, 4, 5, 6, 3, 11, 5, 2, 4, 3, 8, 10, 9, 12, 11, 4, 8, 11, 9, 6, 5, 6, 3, 5, 2, 6, 3, 8, 10, 9, 12, 11, 4, 8, 10, 9, 4, 5, 6, 3, 11, 5, 2, 4, 3, 8}
    )),

    SIZE_11(new BoardConfig(
        11,
        Map.of(
            TileType.wood, 19,
            TileType.clay, 15,
            TileType.wool, 19,
            TileType.wheat, 19,
            TileType.stone, 14,
            TileType.desert, 5
        ),
        Map.of(
            TileType.wood, 1,
            TileType.clay, 1,
            TileType.wool, 1,
            TileType.wheat, 1,
            TileType.stone, 1,
            TileType.desert, 4
        ),
        new int[] {0, 0, 6, 10, 10, 10, 9, 0, 9, 9, 9, 9, 5},
        new int [] {5, 2, 6, 3, 8, 10, 9, 12, 11, 4, 8, 10, 9, 4, 5, 6, 3, 11, 5, 2, 4, 3, 8, 10, 9, 12, 11, 4, 8, 11, 9, 6, 5, 6, 3, 5, 2, 6, 3, 8, 10, 9, 12, 11, 4, 8, 10, 9, 4, 5, 6, 3, 11, 5, 2, 4, 3, 8, 5, 2, 6, 3, 8, 10, 9, 12, 11, 4, 8, 10, 9, 4, 5, 6, 3, 11, 5, 2, 4, 3, 8, 10, 9, 12, 11, 4}
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

