package com.example.hexgame.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Path {
    private Node[] nodes;
    private Player owner;
    private String color;

    public Path() {
        nodes = new Node[2];
        color = "beige";
    }

    public boolean canBuildRoad(Player player) {
        if (owner != null || !"beige".equals(color)) return false;
        //TODO check if road is nearby
        return true;
    }

    public boolean canBuildFreeRoad(Player player) {
        if (owner != null || !"beige".equals(color)) return false;
        //TODO check if village is nearby
        return true;
    }

    @JsonIgnore
    public Node getNodes(int index) {
        return nodes[index];
    }
    public void setNode(int index, Node node) {
        if (index >= nodes.length || nodes[index] != null) return;
        nodes[index] = node;
    }
    public Player getOwner() {
        return owner;
    }

    public void buildRoad(Player owner) {
        if (this.owner != null) return;
        if (owner == null) return;
        this.owner = owner;
        this.color = owner.getColor();
    }

    public String getColor() {
        return color;
    }
}
