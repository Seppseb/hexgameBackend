package com.example.hexgame.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Tile implements Serializable {

    private final int number;
    private final TileType type;
    private Node[] nodes;

    private Robber robber;

    private int rowIndex;
    private int colIndex;

    public Tile(TileType type, int number, Board board, int rowIndex, int colIndex) {
        this.type = type;
        this.number = number;
        this.nodes = new Node[6];
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        if (type == TileType.desert && board.getRobber() == null) {
            this.robber = new Robber(this);
            board.setRobber(this.robber);
        }
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


    public void handleDiceThrow() {
        if (this.hasRobber()) return;
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

    @JsonProperty("hasRobber")
    public boolean hasRobber() {
        return this.robber != null;
    }

    public boolean moveRobber(Tile newTile) {
        if (!this.hasRobber()) return false;
        if (newTile.hasRobber()) return false;
        newTile.robber = this.robber;
        this.robber.setLocation(newTile);
        this.robber = null;
        return true;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    @JsonIgnore
    public Node[] getNodes() {
        return nodes;
    }

    

}
