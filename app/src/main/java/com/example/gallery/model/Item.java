package com.example.gallery.model;

import java.io.Serializable;

public class Item implements Serializable {
    private int drawableVal;
    public Item(int drawableVal){
        this.drawableVal = drawableVal;
    }

    public void setDrawableVal(int drawableVal) {
        this.drawableVal = drawableVal;
    }

    public int getDrawableVal() {
        return drawableVal;
    }
}
