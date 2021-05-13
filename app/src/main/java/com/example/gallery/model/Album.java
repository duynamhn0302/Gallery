package com.example.gallery.model;


import com.example.gallery.activity.MainActivity;

import java.io.Serializable;
import java.util.ArrayList;

public class Album implements Serializable {
    private String name;
    private Item mainImage;
    private String path;
    private int type = 0;
    final static public int typeLove = 1;
    final static public int typePrivate = 2;
    private ArrayList<Item> items = new ArrayList<>();
    public Album(String name){
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Album(String name, Item mainImage){
        this.name = name;
        this.mainImage = mainImage;
        items.add(mainImage);
    }

    public void removeItem(Item item){
        items.remove(item);
        MainActivity.items.remove(item);
    }
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void addHead(Item item){
        items.add(0, item);
    }
    public void addItem(Item item){
        items.add(item);
        if (mainImage == null){
            mainImage = item;
            int i = item.getFilePath().lastIndexOf('/');
            path = item.getFilePath().substring(0, i + 1);
        }
    }
    static public Album getAlbumOf(Item item, ArrayList<Album> albums){
        for(Album album:albums){
            if (album.getImages().contains(item))
                return album;
        }
        return null;
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
