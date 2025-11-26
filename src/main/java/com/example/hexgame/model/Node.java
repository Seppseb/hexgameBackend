package com.example.hexgame.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Node {
    private Player owner;
    private String color;
    private int buildFactor;
    private Tile[] tiles;
    private Node[] neighbours;
    private Path[] paths;
 
    public Node() {
        owner = null;
        color = "beige";
        buildFactor = 0;
        tiles = new Tile[3];
        neighbours = new Node[3];
        paths = new Path[3];
    }

    public Player getOwner() {
        return owner;
    }

    public boolean canBuildVillage(Player player) {
        //TODO check if road is connected
        if (owner != null || !"beige".equals(color)) return false;
        for (Node neighbour : neighbours) {
            if (neighbour != null && neighbour.getOwner() != null) return false;
        }
        return true;
    }

    public boolean canBuildFreeVillage(Player player) {
        if (owner != null || !"beige".equals(color)) return false;
        for (Node neighbour : neighbours) {
            if (neighbour != null && neighbour.getOwner() != null) return false;
        }
        return true;
    }

    public boolean buildVillage(Player player) {
        if(!canBuildVillage(player)) return false;
        owner = player;
        color = player.getColor();
        buildFactor++;
        for (Node neighbour : neighbours) {
            if (neighbour != null) neighbour.markUnuseable();
        }
        return true;
    }

    public boolean canBuildCity(Player player) {
        if (owner != player) return false;
        return true;
    }

    public boolean buildCity(Player player) {
        if (!canBuildCity(player)) return false;
        buildFactor++;
        return true;
    }

    public int getBuildFactor() {
        return buildFactor;
    }

    @JsonIgnore
    public Tile getTile(int index) {
        return tiles[index];
    }

    @JsonIgnore
    public Node getNeighbour(int index) {
        return neighbours[index];
    }

    @JsonIgnore
    public Path getPath(int index) {
        return paths[index];
    }

    public String getColor() {
        return color;
    }

    public void markUnuseable() {
        color = null;
    }

    public void setTile(int index, Tile tile) {
        if (index >= tiles.length || tiles[index] != null) {
            throw new java.lang.Error("bad index");
        }
        tiles[index] = tile;
    }

    public void setNeighbour(int index, Node neighbour) {
        if (index >= neighbours.length || neighbours[index] != null) {
            throw new java.lang.Error("bad index");
        }
        neighbours[index] = neighbour;
    }

    public void setPath(int index, Path path) {
        if (index >= paths.length || paths[index] != null) {
            throw new java.lang.Error("bad index");
        }
        paths[index] = path;
    }

}
