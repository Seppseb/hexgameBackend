package com.example.hexgame.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

// Keep board serializable for JSON transport. Add fields you need.
public class Board implements Serializable {

    private final Random random;
    private final BoardConfig config;

    private Map<TileType, Integer> tileCounts;
    private Map<TileType, Integer> portCounts;

    private final int[] defaultNumberOrder;
    private int[] numberQuantity;
    int[] tilesPerRow;
    int[] nodesPerRow;//TODO check if new generation is correct nodes and paths = new int[] {3, 4, 4, 5, 5, 6, 6, 5, 5, 4, 4, 3};
    int[] pathsPerRow;// = new int[] {6, 4, 8, 5, 10, 6, 10, 5, 8, 4, 6};


    private Tile [][] tiles;
    private Map<Integer, List<Tile>> tilesWithNumber;
    private Node [][] nodes;
    private Path [][] paths;

    private LongestRoadCard longestRoadCard;
    private Robber robber;

    private double numberUnFairnessScore = 0;

    public Board(Random random, int numberOrderConfig, int boardSize) {
        this.random = random;
        this.config = BoardConfigs.fromSize(boardSize);
        this.tileCounts = new HashMap<>(this.config.getTileCounts());
        this.portCounts = new HashMap<>(this.config.getPortCounts());
        this.numberQuantity = this.config.getNumberQuantity().clone();
        this.defaultNumberOrder = this.config.getDefaultNumberOrder();

        this.longestRoadCard = new LongestRoadCard();

        this.tilesPerRow = new int[boardSize];
        int edgeSize = (boardSize + 1) / 2;
        for (int i = 0; i < edgeSize; i++) {
            tilesPerRow[i] = i + edgeSize;
            tilesPerRow[boardSize-1-i] = i + edgeSize;
        }
        int nodeSize = 2 * (boardSize + 1);
        this.nodesPerRow = new int[nodeSize];
        for (int i = 0; i < nodeSize / 2; i++) {
            int nodeAmmount = edgeSize + (i + 1) / 2;
            this.nodesPerRow[i] = nodeAmmount;
            this.nodesPerRow[nodeSize - 1 - i] = nodeAmmount;
        }
        int pathSize = nodeSize - 1;
        this.pathsPerRow = new int[pathSize];
        for (int i = 0; i < edgeSize; i++) {
            int i1 = 2 * i;
            int i2 = i1 + 1;
            pathsPerRow[i1] = (i + edgeSize) * 2;
            pathsPerRow[i2] = i + edgeSize + 1;
            pathsPerRow[pathSize-1-i1] = (i + edgeSize) * 2;
            pathsPerRow[pathSize-1-i2] = i + edgeSize + 1;
        }

        //TODO fix generation for other sizes -> stop size 3?

        generateTiles(); //TODO set type number for eacch size
        generateNodes();
        generatePaths();
        generatePorts();

        switch (numberOrderConfig) {
            case 0:
                //use fixed order
                setNumbersInCircle(this.defaultNumberOrder);
                break;
            case 1:
                //use random order
                generateRandomNumbers();
                break;
            case 2:
                //use best random order of n tries
                List<Integer> fairestNumbers =  generateRandomNumbers();
                double bestScore = this.getNumberUnFairnessScore();
                for (int i = 0; i < 200; i++) {
                    List<Integer> currentNumbers = generateRandomNumbers();
                    double currentScore = this.getNumberUnFairnessScore();
                    System.out.println(currentScore);
                    if (currentScore < bestScore) {
                        fairestNumbers = currentNumbers;
                        bestScore = currentScore;
                    }
                }
                setNumbers(fairestNumbers);
                break;
            default:
                break;
        }

        createNumberToTileMap();
        
    }

    private void generateTiles() {
        this.tiles = new Tile[tilesPerRow.length][];
        for (int i = 0; i < tilesPerRow.length; i++) {
            Tile[] row = new Tile[tilesPerRow[i]];
            for (int k = 0; k < row.length; k++) {
                TileType type = drawType();
                Tile tile = new Tile(type, 0, this, i, k);
                row[k] = tile;
            }
            tiles[i] = row;
        }
    }

    private List<Integer> generateRandomNumbers() {
        this.numberUnFairnessScore = 0;
        int[] avaliableNumbers = this.numberQuantity.clone();
        List<Integer> generatedNumbers = new ArrayList<Integer>();
        for (int i = 0; i < tilesPerRow.length; i++) {
            for (int k = 0; k < tilesPerRow[i]; k++) {
                Tile tile = tiles[i][k];
                int number = tile.getType() == TileType.desert ? 0 : drawRandomNumber(avaliableNumbers);
                tile.setNumber(number);
                generatedNumbers.add(number);
            }
        }
        return generatedNumbers;
    }

    private void setNumbersInCircle(int[] numbers) {
        int numberIndex = 0;
        int rows = tiles.length;
        for (int distanceBorder = 0; distanceBorder * 2 < rows; distanceBorder++) {
            for (int k = distanceBorder; k < tiles[distanceBorder].length - distanceBorder; k++) {
                int i = distanceBorder;
                if (setTileNumber(tiles[i][k], numbers, numberIndex)) numberIndex++;
            }
            for (int i = distanceBorder + 1; i < rows - distanceBorder; i++) {
                int k = tiles[i].length - 1 - distanceBorder;
                if (setTileNumber(tiles[i][k], numbers, numberIndex)) numberIndex++;
            }
            for (int k = tiles[rows-1-distanceBorder].length -2 - distanceBorder; k >= distanceBorder; k--) {
                int i = rows - 1 - distanceBorder;
                if (setTileNumber(tiles[i][k], numbers, numberIndex)) numberIndex++;
            }
            for (int i = rows - 2 - distanceBorder; i > distanceBorder; i--) {
                int k = distanceBorder;
                if (setTileNumber(tiles[i][k], numbers, numberIndex)) numberIndex++;
            }
        }
    }

    private boolean setTileNumber(Tile tile, int[] numbers, int numberIndex) {
        if (tile.getType() == TileType.desert) {
            tile.setNumber(0);
            return false;
        }
        tile.setNumber(numbers[numberIndex]);
        return true;
    }

    private void setNumbers(List<Integer> numbers) {
        int numberIndex = 0;
        for (int i = 0; i < tilesPerRow.length; i++) {
            for (int k = 0; k < tilesPerRow[i]; k++) {
                Tile tile = tiles[i][k];
                tile.setNumber(numbers.get(numberIndex));
                numberIndex++;
            }
        }
    }

    private void createNumberToTileMap() {
        this.tilesWithNumber = new HashMap<>();
        for (int i = 0; i < tilesPerRow.length; i++) {
            for (int k = 0; k < tilesPerRow[i]; k++) {
                Tile tile = tiles[i][k];
                int number = tile.getNumber();
                if (!tilesWithNumber.containsKey(number)) tilesWithNumber.put(number, new ArrayList<Tile>());
                tilesWithNumber.get(number).add(tile);
            }
        }
    }

    private void generateNodes() {
        int nodeRows = nodesPerRow.length;
        nodes = new Node[nodeRows][];
        for (int i = 0; i < nodeRows; i++) {
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
                    if (i < nodeRows / 2) {
                        Tile bot = tiles[i/2][k];
                        bot.setNode(0 , node);
                        node.setTile(1, bot);
                    } else if (i < nodeRows - 2) {
                        if (k >= 1 && k <= tiles[i/2].length) {
                            Tile bot = tiles[i/2][k-1];
                            bot.setNode(0 , node);
                            node.setTile(1, bot);
                        }
                    }
                } else {
                    // 2 top conn
                    int topTileI = (i - 3) / 2;
                    if (i < nodeRows / 2) {
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

                        if (i < nodeRows - 1) {
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
        int pathRows = pathsPerRow.length;
        paths = new Path[pathRows][];
        for (int i = 0; i < pathRows; i++) {
            Path[] row = new Path[pathsPerRow[i]];
            for (int k = 0; k < row.length; k++) {
                Path path = new Path();
                if (i % 2 == 0) {
                    // horrizontal
                    if (i < pathRows / 2) {
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
        int totalPorts = portCounts.values().stream().filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
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
        int totalTypeCount = tileCounts.values().stream().filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        if (totalTypeCount == 0) throw new java.lang.Error("BUG: drew too many types");
        // 0 - totalTypes;
        int typeNumber = random.nextInt(totalTypeCount) + 1;
        int limit = 0;
        for (TileType type: tileCounts.keySet()) {
            limit+= tileCounts.get(type);
            if (typeNumber <= limit) {
                if (tileCounts.get(type) < 1) throw new java.lang.Error("BUG: took type that doesnt exist");
                tileCounts.put(type , tileCounts.get(type) - 1);
                return type;
            }
        }
        throw new java.lang.Error("BUG: no tiles left idiot dev");
    }

    private int drawRandomNumber(int[] avaliableNumbers) {//TODO
        int total = 0;
        for (int i = 0; i < avaliableNumbers.length; i++) {
            total += avaliableNumbers[i];
        }
        if (total < 1) throw new java.lang.Error("BUG: no numbers left idiot config");
        // 0 - total;
        int randomNumber = random.nextInt(total) + 1;
        int limit = 0;
        for (int i = 0; i < avaliableNumbers.length; i++) {
            limit += avaliableNumbers[i];
            if (randomNumber <= limit) {
                if (avaliableNumbers[i] < 1) throw new java.lang.Error("BUG: took number that doesnt exist");
                avaliableNumbers[i]--;
                return i;
            }
        }
        throw new java.lang.Error("BUG: no numbers left idiot dev");
    }

    private TileType drawPortType() {
        int totalPortTypes = portCounts.values().stream().filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        if (totalPortTypes == 0) throw new java.lang.Error("BUG: drew too many port types");
        // 0 - totalPortTypes;
        int typeNumber = random.nextInt(totalPortTypes) + 1;
        int limit = 0;
        for (TileType type: portCounts.keySet()) {
            limit+= portCounts.get(type);
            if (typeNumber <= limit) {
                if (portCounts.get(type) < 1) throw new java.lang.Error("BUG: took port type that doesnt exist");
                portCounts.put(type, portCounts.get(type) - 1);
                if (type == TileType.desert) return null;
                return type;
            }
        }
        throw new java.lang.Error("BUG: no ports left idiot dev");
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

    public Robber getRobber() {
        return robber;
    }

    public void setRobber(Robber robber) {
        this.robber = robber;
    }

    public double getNumberUnFairnessScore() {
        if (this.numberUnFairnessScore != 0) return this.numberUnFairnessScore;

        double score = 0;
        for (int i = 0; i < nodes.length; i++) {
            Node[] row = nodes[i];
            for (int k = 0; k < row.length; k++) {
                Node node = row[k];
                double totalProb = 0;
                for (int n = 0; n < 3; n++) {
                    Tile tile = node.getTile(n);
                    if (tile == null || tile.getType() == TileType.desert) {
                        continue;
                    }
                    totalProb += diceProbability(tile.getNumber());
                }
                score += Math.pow(totalProb, 4);
            }
        }
        this.numberUnFairnessScore = score;
        return this.numberUnFairnessScore;
    }

    private int diceProbability(int n) {
        if (n > 12 || n < 2) return 0;
        if (n > 7) {
            n-= 2 * (n-7);
        }
        return n-1;
    }

}
