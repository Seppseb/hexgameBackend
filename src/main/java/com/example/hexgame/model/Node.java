package com.example.hexgame.model;

import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Node {
    private Player owner;
    private String color;
    private int buildFactor;
    private Tile[] tiles;
    private Node[] neighbours;
    private Path[] paths;
    private Port port;

    private HashSet<String> canPlaceVillage;
 
    public Node() {
        owner = null;
        color = "beige";
        buildFactor = 0;
        tiles = new Tile[3];
        neighbours = new Node[3];
        paths = new Path[3];
        canPlaceVillage = new HashSet<String>();
    }

    public Player getOwner() {
        return owner;
    }

    public boolean canBuildVillage(Player player) {
        if (owner != null || !"beige".equals(color) || player == null) return false;
        if (!canPlaceVillage.contains(player.getUserId())) return false;
        for (Node neighbour : neighbours) {
            if (neighbour != null && neighbour.getOwner() != null) return false;
        }
        return true;
    }

    public boolean canBuildInitialVillage(Player player) {
        if (owner != null || !"beige".equals(color) || player == null) return false;
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
        if (port != null) {
            player.buildPort(port.getType());
        }
        this.canPlaceVillage.clear();
        for (Node neighbour : neighbours) {
            if (neighbour != null) neighbour.markUnuseable();
        }
        for (Path path: paths) {
            if (path != null) path.claimNode(player, this);
        }
        return true;
    }

    public boolean buildInitialVillage(Player player, boolean getRessources) {
        if(!canBuildInitialVillage(player)) return false;
        owner = player;
        color = player.getColor();
        buildFactor++;
        if (port != null) {
            player.buildPort(port.getType());
        }
        for (Node neighbour : neighbours) {
            if (neighbour != null) neighbour.markUnuseable();
        }
        for (Path path: paths) {
            if (path != null) path.claimNode(player, this);
        }

        letPlayerBuildInitialRoad(player);
        
        if (getRessources) {
            for (int i = 0; i < tiles.length; i++) {
                if (tiles[i] != null) {
                    tiles[i].handleInitialBuild(player);
                }
            }
        }
        return true;
    }

    public void builtAdjacentRoad(Player player, Path origin) {
        for (Path path: paths) {
            if (path == null) continue;
            if (path == origin) continue;
            path.addReachable(player.getUserId(), this);
        }
        if (owner != null || !"beige".equals(color) || player == null) return;
        for (Node neighbour : neighbours) {
            if (neighbour != null && neighbour.getOwner() != null) return;
        }
        this.canPlaceVillage.add(player.getUserId());
    }

    public void letPlayerBuildInitialRoad(Player player) {
        for (Path path: paths) {
            if (path == null) continue;
            if (path.getOwner() != null) continue;
            path.addInitialRoadPlacer(player.getUserId());
        }
    }

    public void removeOtherInitialRoadPlaceSpots(Player player) {
        for (Path path: paths) {
            if (path == null) continue;
            path.removeInitialRoadPlacer(player.getUserId());
        }
    }

    public boolean canBuildCity(Player player) {
        if (owner != player) return false;
        if (buildFactor != 1) return false;
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

    public HashSet<String> getCanPlaceVillage() {
        return canPlaceVillage;
    }

    public void markUnuseable() {
        color = null;
        this.canPlaceVillage.clear();
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

    public void handleDiceThrow(TileType type) {
        if (owner != null) {
            owner.addRes(type, buildFactor);
        }
    }

    public Port getPort() {
        return port;
    }

    public void setPort(Port port) {
        this.port = port;
    }

}
