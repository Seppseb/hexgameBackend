package com.example.hexgame.dto;

import java.util.HashMap;

import com.example.hexgame.model.Bank;
import com.example.hexgame.model.TileType;

public class BankDTO {

    public HashMap<TileType, Integer> resBalance;
    public int numberDevelopments;
    public boolean showExactRes;
    
    public BankDTO(Bank b) {
        this.numberDevelopments = b.getNumberDevelopments();
        this.showExactRes = b.getShowExactRes();

        this.resBalance = new HashMap<>();
        HashMap<TileType, Integer> rb = b.getResBalance();
        for (TileType tt: rb.keySet()) {
            if (this.showExactRes) {
                this.resBalance.put(tt, rb.get(tt));
            } else {
                if (rb.get(tt) > 0) {
                    this.resBalance.put(tt, 1);
                } else {
                    this.resBalance.put(tt, 0);
                }
            }
        }
    }
}
