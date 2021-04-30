package com.example.gallery.model;


import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class Video extends Item implements Serializable {
    private String duration;

    public Video(Long id, String absolutePathOfFile, String addedDate, String convertToDuration, String location) {
        this.id = id;
        this.filePath = absolutePathOfFile;
        this.isImage = false;
        this.addedDate = addedDate;
        this.duration = convertToDuration;
        this.location = location;
    }

    public Video(Uri uri) {
    }

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
