package com.example.hexgame.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tile implements Serializable {

    private final int number;
    private final TileType type;
    private List<Node> nodes;

    public Tile(TileType type, int number) {
        this.type = type;
        this.number = number;
        this.nodes = new ArrayList<>();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public int getNumber() {
        return number;
    }

    public TileType getType() {
        return type;
    }

    public void handleDiceThrow() {
        for (Node node: nodes) {
            node.handleDiceThrow(this.type);
        }
    }

}
