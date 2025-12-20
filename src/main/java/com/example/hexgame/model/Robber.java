package com.example.hexgame.model;

public class Robber {
    
    private Tile location;

    public Robber(Tile location) {
        this.location = location;
    }

    public Tile getLocation() {
        return location;
    }

    public void setLocation(Tile newLocation) {
        this.location = newLocation;
    }

}
