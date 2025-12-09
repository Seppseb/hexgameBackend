package com.example.hexgame.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Tile implements Serializable {

    private final int number;
    private final TileType type;
    private Node[] nodes;

    public Tile(TileType type, int number) {
        this.type = type;
        this.number = number;
        this.nodes = new Node[6];
    }

    public int getNumber() {
        return number;
    }

    public TileType getType() {
        return type;
    }

    public void setNode(int index, Node node) {
        if (index >= nodes.length || nodes[index] != null) {
            throw new java.lang.Error("bad index");
        }
        nodes[index] = node;
    }

    @JsonIgnore
    public Node getNode(int index) {
        return nodes[index];
    }

    public void handleDiceThrow() {
        for (Node node: nodes) {
            node.handleDiceThrow(this.type);
        }
    }

    public void handleInitialBuild(Player player) {
        player.addRes(this.type, 1);
    }

    public String toString() {
        return type + ", " + number;
    }

}
