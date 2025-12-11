package com.example.hexgame.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Keep board serializable for JSON transport. Add fields you need.
public class Board implements Serializable {

    private Random random;

    private int wood = 4;
    private int clay = 3;
    private int wool = 4;
    private int wheat = 4;
    private int stone = 3;
    private int desert = 1;


    private int woodPorts = 1;
    private int clayPorts = 1;
    private int woolPorts = 1;
    private int wheatPorts = 1;
    private int stonePorts = 1;
    private int generalPorts = 4;


    //TODO make variable

    private int[] numbers = new int[] {0, 0, 1, 2, 2, 2, 2, 0, 2, 2, 2, 2, 1};
    int[] tilesPerRow = new int[]{3, 4, 5, 4, 3};
    int[] nodesPerRow = new int[] {3, 4, 4, 5, 5, 6, 6, 5, 5, 4, 4, 3};
    int[] pathsPerRow = new int[] {6, 4, 8, 5, 10, 6, 10, 5, 8, 4, 6};


    private Tile [][] tiles;
    private Map<Integer, List<Tile>> tilesWithNumber;
    private Node [][] nodes;
    private Path [][] paths;

    private LongestRoadCard longestRoadCard;

    public Board(Random random) {
        this.random = random;

        this.longestRoadCard = new LongestRoadCard();

        generateTiles();
        generateNodes();
        generatePaths();
        generatePorts();
    }

    private void generateTiles() {
        tiles = new Tile[tilesPerRow.length][];
        tilesWithNumber = new HashMap<>();
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
    }

    private void generateNodes() {
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
    }

    private void generatePaths() {
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

    private void generatePorts() {
        //                 upper row         lower row                        side rows without upper or lower
        int numberCoasts = pathsPerRow[0] + pathsPerRow[pathsPerRow.length-1] + 2 * pathsPerRow.length - 4;
        int totalPorts = woodPorts + clayPorts + woolPorts + wheatPorts + stonePorts + generalPorts;
        boolean[] hasPort = distributePorts(numberCoasts, totalPorts);
        int coastIndex = 0;
        //upper row
        for (int i = 0; i < pathsPerRow[0]; i++) {
            Path path = paths[0][i];
            if (hasPort[coastIndex]) {
                path.setPort(new Port(drawPortType()));
            }
            coastIndex++;
        }
        //right side
        for (int i = 1; i < pathsPerRow.length - 1; i++) {
            Path path = paths[i][paths[i].length-1];
            if (hasPort[coastIndex]) {
                path.setPort(new Port(drawPortType()));
            }
            coastIndex++;
        }
        //lower side
        for (int i = pathsPerRow[pathsPerRow.length-1] - 1; i >= 0; i--) {
            Path path = paths[pathsPerRow.length-1][i];
            if (hasPort[coastIndex]) {
                path.setPort(new Port(drawPortType()));
            }
            coastIndex++;
        }
        //left side
        for (int i = pathsPerRow.length - 2; i >= 1; i--) {
            Path path = paths[i][0];
            if (hasPort[coastIndex]) {
                path.setPort(new Port(drawPortType()));
            }
            coastIndex++;
        }
    }

    private boolean[] distributePorts(int numberCoasts, int totalPorts) {
        boolean[] hasPort = new boolean[numberCoasts];
        
        if (totalPorts <= 0 || numberCoasts <= 0) return hasPort;
        
        double step = (double) numberCoasts / totalPorts;
        
        for (int i = 0; i < totalPorts; i++) {
            int pos = (int) Math.round(i * step) % numberCoasts;
            hasPort[pos] = true;
        }
        return hasPort;
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

    private TileType drawPortType() {
        int totalPortTypes = woodPorts + clayPorts + woolPorts + wheatPorts + stonePorts + generalPorts;
        if (totalPortTypes == 0) throw new java.lang.Error("BUG: drew too many port types");
        // 0 - totalPortTypes;
        int typeNumber = random.nextInt(totalPortTypes) + 1;
        int limit = woodPorts;
        if (typeNumber <= limit) {
            if (woodPorts < 1) throw new java.lang.Error("BUG: took port type that doesnt exist");
            woodPorts--;
            return TileType.wood;
        }
        limit+= clayPorts;
        if (typeNumber <= limit) {
            if (clayPorts < 1) throw new java.lang.Error("BUG: took port type that doesnt exist");
            clayPorts--;
            return TileType.clay;
        }
        limit+= woolPorts;
        if (typeNumber <= limit) {
            if (woolPorts < 1) throw new java.lang.Error("BUG: took port type that doesnt exist");
            woolPorts--;
            return TileType.wool;
        }
        limit+= wheatPorts;
        if (typeNumber <= limit) {
            if (wheatPorts < 1) throw new java.lang.Error("BUG: took port type that doesnt exist");
            wheatPorts--;
            return TileType.wheat;
        }
        limit+= stonePorts;
        if (typeNumber <= limit) {
            if (stonePorts < 1) throw new java.lang.Error("BUG: took port type that doesnt exist");
            stonePorts--;
            return TileType.stone;
        }
        if (generalPorts < 1) throw new java.lang.Error("BUG: took port type that doesnt exist");
        generalPorts--;
        return null;
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

    public void checkLongestRoad() {
        HashMap<Player, Integer> newLongestRoad = new HashMap<Player, Integer>();
        int max = 0;
        for (int ri = 0; ri < paths.length; ri++) {
            Path [] row = paths[ri];
            for (int i = 0; i < row.length; i++) {
                Path path = row[i];
                if (path.getOwner() == null) continue;
                HashSet<Path> used = new HashSet<Path>();
                used.add(path);
                int length1 = checkRoadLength(path, used, path.getNodes(0));
                int length2 = checkRoadLength(path, used, path.getNodes(1));
                int length = length1 > length2 ? length1 : length2;
                if (length > max) max = length;
                if (newLongestRoad.get(path.getOwner()) == null || length > newLongestRoad.get(path.getOwner())) {
                    newLongestRoad.put(path.getOwner(), length);
                }
            }
        }

        for (Player player: newLongestRoad.keySet()) {
            player.setLongestRoad(newLongestRoad.get(player));
        }

        this.longestRoadCard.checkOwnerChange(newLongestRoad, max);
    }

    public int checkRoadLength(Path path, HashSet<Path> used, Node lastNode) {
        int max = used.size();
        for (int ni = 0; ni < 2; ni++) {
            Node node = path.getNodes(ni);
            if (node == null) continue;
            if (node == lastNode) continue;
            if (node.getOwner() != null && node.getOwner() != path.getOwner()) continue;
            for (int pi = 0; pi < 3; pi++) {
                Path next = node.getPath(pi);
                if (next == null) continue;
                if (next.getOwner() != path.getOwner()) continue;
                if (used.contains(next)) continue;
                used.add(next);
                int newLength = checkRoadLength(next, used, node);
                max = newLength > max ? newLength : max;
                used.remove(next);
            }
        }
        return max;
    }

}
