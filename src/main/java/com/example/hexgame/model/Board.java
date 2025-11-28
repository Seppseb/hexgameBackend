package com.example.hexgame.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Keep board serializable for JSON transport. Add fields you need.
public class Board implements Serializable {

    private Random random;

    private int wood;
    private int clay;
    private int wool;
    private int wheat;
    private int stone;
    private int desert;

    private int[] numbers;

    private Tile [][] tiles;
    private Map<Integer, List<Tile>> tilesWithNumber;
    private Node [][] nodes;
    private Path [][] paths;

    public Board(Random random) {
        wood = 4;
        clay = 3;
        wool = 4;
        wheat = 4;
        stone = 3;
        desert = 1;
        numbers = new int[] {0, 0, 1, 2, 2, 2, 2, 0, 2, 2, 2, 2, 1};

        this.random = random;
        tilesWithNumber = new HashMap<>();
        
        int[] tilesPerRow = new int[]{3, 4, 5, 4, 3};
        int[] nodesPerRow = new int[] {3, 4, 4, 5, 5, 6, 6, 5, 5, 4, 4, 3};
        int[] pathsPerRow = new int[] {6, 4, 8, 5, 10, 6, 10, 5, 8, 4, 6};


        tiles = new Tile[tilesPerRow.length][];
        for (int i = 0; i < tilesPerRow.length; i++) {
            Tile[] row = new Tile[tilesPerRow[i]];
            for (int k = 0; k < row.length; k++) {
                TileType type = drawType();
                int number = type == TileType.desert ? 0 : drawNumber();
                Tile tile = new Tile(type, number);
                if (!tilesWithNumber.containsKey(number)) tilesWithNumber.put(number, new ArrayList<Tile>());
                tilesWithNumber.get(number).add(tile);
                row[k] = tile;
            }
            tiles[i] = row;
        }

        nodes = new Node[nodesPerRow.length][];
        for (int i = 0; i < nodesPerRow.length; i++) {
            Node[] row = new Node[nodesPerRow[i]];
            for (int k = 0; k < row.length; k++) {
                Node node = new Node();
                if (i % 2 == 0) {
                    // 1 top conn
                    if (i > 0) {
                        Node top = nodes[i-1][k];
                        top.setNeighbour(2, node);
                        node.setNeighbour(0, top);

                        if (k > 0) {
                            Tile topLeft = tiles[i/2 - 1][k-1];
                            node.setTile(2, topLeft);
                            topLeft.setNode(2, node);
                        }
                        if (k < tiles[i/2 - 1].length) {
                            Tile topRigth = tiles[i/2 - 1][k];
                            node.setTile(0, topRigth);
                            topRigth.setNode(4, node);
                        }

                    }
                    // bot tile of even row
                    if (i <= 4) {
                        Tile bot = tiles[i/2][k];
                        bot.setNode(0 , node);
                        node.setTile(1, bot);
                    } else if (i <= 8) {
                        if (k >= 1 && k <= tiles[i/2].length) {
                            Tile bot = tiles[i/2][k-1];
                            bot.setNode(0 , node);
                            node.setTile(1, bot);
                        }
                    }
                } else {
                    // 2 top conn
                    int topTileI = (i - 3) / 2;
                    if (i<=5) {
                        if (k > 0) {
                            Node leftTop = nodes[i-1][k-1];
                            leftTop.setNeighbour(1, node);
                            node.setNeighbour(0, leftTop);
                        }
                        if (k < nodes[i-1].length) {
                            Node rightTop = nodes[i-1][k];
                            rightTop.setNeighbour(2, node);
                            node.setNeighbour(1, rightTop);
                        }
                        if (i >= 3 && k > 0 && k <= tiles[topTileI].length) {
                            Tile top = tiles[topTileI][k-1];
                            top.setNode(3, node);
                            node.setTile(0, top);
                        }
                        if (k > 0) {
                            Tile botLeft = tiles[topTileI + 1][k-1];
                            botLeft.setNode(1, node);
                            node.setTile(2, botLeft);
                        }
                        if (k < tiles[topTileI + 1].length) {
                            Tile botRigth = tiles[topTileI + 1][k];
                            botRigth.setNode(5, node);
                            node.setTile(1, botRigth);
                        }
                    } else {
                        Node leftTop = nodes[i-1][k];
                        Node rightTop = nodes[i-1][k+1];
                        leftTop.setNeighbour(1, node);
                        rightTop.setNeighbour(2, node);
                        node.setNeighbour(0, leftTop);
                        node.setNeighbour(1, rightTop);

                        Tile top = tiles[topTileI][k];
                        top.setNode(3, node);
                        node.setTile(0, top);

                        if (i <= 9) {
                            if (k >= 1) {
                                Tile botLeft = tiles[topTileI+1][k-1];
                                botLeft.setNode(1, node);
                                node.setTile(2, botLeft);
                            }
                            if (k < tiles[topTileI+1].length) {
                                Tile botRight = tiles[topTileI+1][k];
                                botRight.setNode(5, node);
                                node.setTile(1, botRight);
                            }
                        }
                    }
                }
                row[k] = node;
            }
            nodes[i] = row;
        }

        paths = new Path[pathsPerRow.length][];
        for (int i = 0; i < pathsPerRow.length; i++) {
            Path[] row = new Path[pathsPerRow[i]];
            for (int k = 0; k < row.length; k++) {
                Path path = new Path();
                if (i % 2 == 0) {
                    // horrizontal
                    if (i <= 4) {
                        if (k % 2 == 0) {
                            Node left = nodes[i+1][k/2];
                            Node rigth = nodes[i][k/2];
                            path.setNode(0, left);
                            path.setNode(1, rigth);
                            left.setPath(0, path);
                            rigth.setPath(2, path);
                        } else {
                            Node left = nodes[i][(k-1)/2];
                            Node rigth = nodes[i+1][(k+1)/2];
                            path.setNode(0, left);
                            path.setNode(1, rigth);
                            left.setPath(1, path);
                            rigth.setPath(2, path);
                        }
                    } else {
                        if (k % 2 == 1) {
                            Node left = nodes[i+1][(k-1)/2];
                            Node rigth = nodes[i][(k+1)/2];
                            path.setNode(0, left);
                            path.setNode(1, rigth);
                            left.setPath(0, path);
                            rigth.setPath(2, path);
                        } else {
                            Node left = nodes[i][k/2];
                            Node rigth = nodes[i+1][k/2];
                            path.setNode(0, left);
                            path.setNode(1, rigth);
                            left.setPath(1, path);
                            rigth.setPath(2, path);
                        }
                    }
                } else {
                    // vertical
                    Node top = nodes[i][k];
                    Node bot = nodes[i+1][k];
                    path.setNode(0, top);
                    path.setNode(1, bot);
                    top.setPath(1, path);
                    bot.setPath(0, path);
                }
                row[k] = path;
            }
            paths[i] = row;
        }


    }

    private TileType drawType() {
        int totalTypes = wood + clay + wool + wheat + stone + desert;
        if (totalTypes == 0) throw new java.lang.Error("BUG: drew too many types");
        // 0 - totalTypes;
        int typeNumber = random.nextInt(totalTypes) + 1;
        int limit = wood;
        if (typeNumber <= limit) {
            if (wood < 1) throw new java.lang.Error("BUG: took type that doesnt exist");
            wood--;
            return TileType.wood;
        }
        limit+= clay;
        if (typeNumber <= limit) {
            if (clay < 1) throw new java.lang.Error("BUG: took type that doesnt exist");
            clay--;
            return TileType.clay;
        }
        limit+= wool;
        if (typeNumber <= limit) {
            if (wool < 1) throw new java.lang.Error("BUG: took type that doesnt exist");
            wool--;
            return TileType.wool;
        }
        limit+= wheat;
        if (typeNumber <= limit) {
            if (wheat < 1) throw new java.lang.Error("BUG: took type that doesnt exist");
            wheat--;
            return TileType.wheat;
        }
        limit+= stone;
        if (typeNumber <= limit) {
            if (stone < 1) throw new java.lang.Error("BUG: took type that doesnt exist");
            stone--;
            return TileType.stone;
        }
        if (desert < 1) throw new java.lang.Error("BUG: took type that doesnt exist");
        desert--;
        return TileType.desert;
    }

    private int drawNumber() {
        int total = 0;
        for (int i = 0; i < numbers.length; i++) {
            total += numbers[i];
        }
        if (total < 1) throw new java.lang.Error("BUG: no numbers left idiot config");
        // 0 - total;
        int randomNumber = random.nextInt(total) + 1;
        int limit = 0;
        for (int i = 0; i < numbers.length; i++) {
            limit += numbers[i];
            if (randomNumber <= limit) {
                if (numbers[i] < 1) throw new java.lang.Error("BUG: took number that doesnt exist");
                numbers[i]--;
                return i;
            }
        }
        throw new java.lang.Error("BUG: no numbers left idiot dev");
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public Map<Integer, List<Tile>> getTilesWithNumber() {
        return tilesWithNumber;
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < tiles.length; i++) {
            Tile[] row = tiles[i];
            for (int k = 0; k < row.length; k++) {
                s+= row[k].getType().toString() + row[k].getNumber();
            }
            s+= "\n";
        }
        return s;
    }

    public Node[][] getNodes() {
        return nodes;
    }

    public Path[][] getPaths() {
        return paths;
    }

    public void handleDice(int diceNumber) {
        if (diceNumber > 12 || diceNumber < 2) throw new java.lang.Error("BUG: bad dice value");
        if (diceNumber == 7) return;
        List<Tile> tileList = tilesWithNumber.get(diceNumber);
        for (Tile tile: tileList) {
            tile.handleDiceThrow();
        }
    }

}
