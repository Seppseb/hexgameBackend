package com.example.hexgame.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Path {
    private Node[] nodes;
    private Player owner;
    private String color;

    private Port port;

    private HashMap<String, HashSet<Node>> reachableForPlayerViaNode;
    private HashSet<String> canPlaceInitialRoad;

    public Path() {
        nodes = new Node[2];
        color = "beige";
        reachableForPlayerViaNode = new HashMap<String, HashSet<Node>>();
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
        if (!reachableForPlayerViaNode.containsKey(player.getUserId())) return false;
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
        this.canPlaceInitialRoad.clear();
        for (Node node: nodes) {
            if (node == null) return;
            node.builtAdjacentRoad(owner, this);
        }
    }

    public void buildInitialRoad(Player owner) {
        if (!canBuildInitialRoad(owner)) return;
        this.owner = owner;
        this.color = owner.getColor();
        this.canPlaceInitialRoad.clear();
        for (Node node: nodes) {
            if (node == null) return;
            node.removeOtherInitialRoadPlaceSpots(owner);
            node.builtAdjacentRoad(owner, this);
        }
    }

    public String getColor() {
        return color;
    }

    public void addReachable(String playerId, Node node) {
        if (!reachableForPlayerViaNode.containsKey(playerId)) {
            reachableForPlayerViaNode.put(playerId, new HashSet<Node>());
        }
        reachableForPlayerViaNode.get(playerId).add(node);
    }
    
    public void addInitialRoadPlacer(String playerId) {
        if (owner != null) return;
        this.canPlaceInitialRoad.add(playerId);
    }

    public void removeInitialRoadPlacer(String playerId) {
        this.canPlaceInitialRoad.remove(playerId);
    }

    public Set<String> getCanPlaceRoad() {
        return reachableForPlayerViaNode.keySet();
    }

    public HashMap<String, HashSet<Node>> getReachableForPlayerViaNode() {
        return reachableForPlayerViaNode;
    }

    public HashSet<String> getCanPlaceInitialRoad() {
        return canPlaceInitialRoad;
    }

    public void claimNode(Player owner, Node node) {
        HashSet<String> deleted = new HashSet<String>();
        for (String playerId: reachableForPlayerViaNode.keySet()) {
            if (owner.getUserId().equals(playerId)) continue;
            reachableForPlayerViaNode.get(playerId).remove(node);
            if (reachableForPlayerViaNode.get(playerId).size() <= 0) {
                deleted.add(playerId);
                //TODO? recursivly remove otherreachable nodes and paths reachble via this node
            }
        }
        for (String playerId: deleted) {
            reachableForPlayerViaNode.remove(playerId);
        }
    }


    public Port getPort() {
        return port;
    }

    public void setPort(Port port) {
        this.port = port;
        for (Node node: nodes) {
            if (node != null) node.setPort(port);
        }
    }
    
}
