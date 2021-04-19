package com.example.gallery.model;


import android.net.Uri;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class Video extends Item implements Serializable {
    private String duration;
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Video video = (Video) obj;
        return filePath.equals(((Video) obj).getFilePath());
    }

    public Video() {
        isImage = false;
    }
    public Video(Uri uri){
        super();
        isImage = false;
    }
    public Video(String filePath){
        this.filePath = filePath;
        super.isImage = false;
    }

    public Video(String filePath,  String addedDate){
        this.filePath = filePath;
        this.isImage = false;
        this.addedDate = addedDate;
        this.duration = "0:0";
    }

    public Video(String filePath,  String addedDate, String duration){
        this.filePath = filePath;
        this.isImage = false;
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
