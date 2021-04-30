package com.example.gallery.model;


import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class Image extends Item implements Serializable {

    public Image(Long id, String absolutePathOfFile, String addedDate, String location) {
        this.id = id;
        this.filePath = absolutePathOfFile;
        super.isImage = true;
        this.addedDate = addedDate;
        this.location = location;
    }

    public Image(Uri uri) {
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Image image = (Image) obj;
        return filePath.equals(((Image) obj).getFilePath());
    }

    public Image() {
        isImage = true;
    }

    public Image(String filePath){
        this.filePath = filePath;
        super.isImage = true;
    }
    public Image(String filePath, String addedDate){
        this.filePath = filePath;
        super.isImage = true;
        this.addedDate = addedDate;
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


}
