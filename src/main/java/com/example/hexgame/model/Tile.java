package com.example.hexgame.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tile implements Serializable {

    private final int number;
    private final String type;
    private List<Node> nodes;

    public Tile(String type, int number) {
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

    public String getType() {
        return type;
    }

}
