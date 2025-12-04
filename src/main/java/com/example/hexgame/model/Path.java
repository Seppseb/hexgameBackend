package com.example.hexgame.model;

import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Path {
    private Node[] nodes;
    private Player owner;
    private String color;

    private HashSet<String> canPlaceRoad;
    private HashSet<String> canPlaceInitialRoad;

    public Path() {
        nodes = new Node[2];
        color = "beige";
        canPlaceRoad = new HashSet<String>();
        canPlaceInitialRoad = new HashSet<String>();
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

    public boolean canBuildRoad(Player player) {
        if (owner != null || !"beige".equals(color) || player == null) return false;
        if (!canPlaceRoad.contains(player.getUserId())) return false;
        return true;
    }

    public boolean canBuildInitialRoad(Player player) {
        if (owner != null || !"beige".equals(color) || player == null) return false;
        if (!canPlaceInitialRoad.contains(player.getUserId())) return false;
        return true;
    }

    public void buildRoad(Player owner) {
        if (!canBuildRoad(owner)) return;
        this.owner = owner;
        this.color = owner.getColor();
        this.canPlaceRoad.clear();
        this.canPlaceInitialRoad.clear();
        for (Node node: nodes) {
            if (node == null) return;
            node.builtAdjacentRoad(owner);
        }
    }

    public void buildInitialRoad(Player owner) {
        if (!canBuildInitialRoad(owner)) return;
        this.owner = owner;
        this.color = owner.getColor();
        this.canPlaceRoad.clear();
        this.canPlaceInitialRoad.clear();
        for (Node node: nodes) {
            if (node == null) return;
            node.removeOtherInitialRoadPlaceSpots(owner);
            node.builtAdjacentRoad(owner);
        }
    }

    public String getColor() {
        return color;
    }

    public void addRoadPlacer(String playerId) {
        this.canPlaceRoad.add(playerId);
    }
    
    public void addInitialRoadPlacer(String playerId) {
        if (owner != null) return;
        this.canPlaceInitialRoad.add(playerId);
    }

    public void removeInitialRoadPlacer(String playerId) {
        this.canPlaceInitialRoad.remove(playerId);
    }

    public HashSet<String> getCanPlaceRoad() {
        return canPlaceRoad;
    }

    public HashSet<String> getCanPlaceInitialRoad() {
        return canPlaceInitialRoad;
    }
    
}
