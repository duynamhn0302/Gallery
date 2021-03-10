package com.example.gallery.model;


import java.io.Serializable;
import java.util.ArrayList;

public class Album implements Serializable {
    private String name;
    private Item mainItem;
    private ArrayList<Item> items = new ArrayList<>();
    public Album(String name){
        this.name = name;
    }
    public Album(String name,Item mainItem){
        this.name = name;
        this.mainItem = mainItem;
        items.add(mainItem);
    }

    public void addItem(Item item){
        items.add(item);
    }

    public Item getMainItem() {
        return mainItem;
    }

    public void setMainItem(Item mainItem) {
        this.mainItem = mainItem;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public String getName() {
        return name;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public void setName(String name) {
        this.name = name;
    }
    public int getCount(){
        return items.size();
    }

}
