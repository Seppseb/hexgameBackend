package com.example.hexgame.model;

import java.util.HashMap;
import java.util.HashSet;

public class LongestRoadCard {
    
    private Player owner;
    //TODO make varialbe later?
    private final int minLimit = 5;

    public LongestRoadCard() {
        this.owner = null;
    }

    public void checkOwnerChange(HashMap<Player, Integer> newLongestRoad, int max) {

        HashSet<Player> hasLongestRoad = new HashSet<Player>();

        for (Player player: newLongestRoad.keySet()) {
            if (newLongestRoad.get(player) == max) hasLongestRoad.add(player);
        }

        if (max < this.minLimit) {
            //no one can get card
            //if someone has card, remove it, reset limit
            setNewOwner(null);
        } else {
            //card could be given
            if (!hasLongestRoad.contains(this.owner)) {
                //and we have a new owner
                if (hasLongestRoad.size() > 1) {
                    //but there a multiple canidates -> no one gets it
                    setNewOwner(null);
                } else if (hasLongestRoad.size() == 1) {
                    //and there is only one new owner
                    for (Player newOwner : hasLongestRoad) {
                        setNewOwner(newOwner);
                    }
                } else {
                    //no new owner, error?
                    setNewOwner(null);
                }
            }
        }

    }

    private void setNewOwner(Player newOwner) {
        if (this.owner == newOwner) return;
        if (this.owner != null) {
            this.owner.addVictoryPoints(-2);
        }
        if (newOwner != null) {
            newOwner.addVictoryPoints(2);
        }
        this.owner = newOwner;
    }

}
