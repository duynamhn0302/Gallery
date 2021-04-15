package com.example.gallery.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class Item implements Serializable {
    private String filePath, addedDate, duration;
    private boolean isImage;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Item item = (Item) obj;
        return filePath.equals(((Item) obj).getFilePath());
    }

    public Item() {

    }

    public Item(String filePath, boolean isImage){
        this.filePath = filePath;
        this.isImage = isImage;
    }

    public Item(String filePath, boolean isImage, String addedDate){
        this.filePath = filePath;
        this.isImage = isImage;
        this.addedDate = addedDate;
        this.duration = "0:0";
    }

    public Item(String filePath, boolean isImage, String addedDate, String duration){
        this.filePath = filePath;
        this.isImage = isImage;
        this.addedDate = addedDate;
        this.duration = duration;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setIsImage(boolean isImage) { this.isImage = isImage; }

    public String getFilePath() {
        return filePath;
    }

    public boolean getIsImage() { return isImage; }

    public void setAddedDate(String addedDate) { this.addedDate = addedDate; }

    public String getAddedDate() { return  addedDate; }

    public void setDuration(String duration) { this.duration = duration; }

    public String getDuration() { return this.duration; }
}
