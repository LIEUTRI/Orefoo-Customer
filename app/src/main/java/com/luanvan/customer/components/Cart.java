package com.luanvan.customer.components;

import java.util.ArrayList;
import java.util.HashMap;

public class Cart {
    private int id;
    private ArrayList<Victual> victuals;
    private HashMap<Integer,Integer> quantityVictuals;
    public Cart(){}
    public Cart(int id, ArrayList<Victual> victuals, HashMap<Integer,Integer> quantityVictuals){
        this.id = id;
        this.victuals = victuals;
        this.quantityVictuals = quantityVictuals;
    }

    public HashMap<Integer,Integer> getQuantityVictuals() {
        return quantityVictuals;
    }

    public void setQuantityVictuals(HashMap<Integer,Integer> quantityVictuals) {
        this.quantityVictuals = quantityVictuals;
    }

    public int getId() {
        return id;
    }

    public ArrayList<Victual> getVictuals() {
        return victuals;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setVictuals(ArrayList<Victual> victuals) {
        this.victuals = victuals;
    }
}
