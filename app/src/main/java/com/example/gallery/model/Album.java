package com.example.gallery.model;


import java.io.Serializable;
import java.util.ArrayList;

public class Album implements Serializable {
    private String name;
    private Item mainImage;
    private ArrayList<Item> items = new ArrayList<>();
    public Album(String name){
        this.name = name;
    }
    public Album(String name, Item mainImage){
        this.name = name;
        this.mainImage = mainImage;
        items.add(mainImage);
    }

    public void addItem(Image image){
        items.add(image);
    }

    public Item getMainImage() {
        return mainImage;
    }

    public void setMainImage(Item mainImage) {
        this.mainImage = mainImage;
    }

    public ArrayList<Item> getImages() {
        return items;
    }

    public String getName() {
        return name;
    }

    public void setImages(ArrayList<Item> images) {
        this.items = images;
    }

    public void setName(String name) {
        this.name = name;
    }
    public int getCount(){
        return items.size();
    }

}
