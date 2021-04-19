package com.example.gallery.model;


import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import java.io.Serializable;

public abstract class Item implements Serializable {

    protected String filePath, addedDate, location;
    protected Uri uri;
    protected boolean isImage;
    Item(){

    }
    Item(Uri uri){
        this.uri = uri;
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Item item = (Item) obj;
        return filePath.equals(((Item) obj).getFilePath());
    }
    public void setAddedDate(String addedDate) {
        this.addedDate = addedDate;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getLocation() {
        return location;
    }

    public Uri getUri() {
        return uri;
    }

    public String getAddedDate() {
        return addedDate;
    }


    public String getFilePath() {
        return filePath;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        isImage = image;
    }


    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public static Item getInstance(final Context context, Uri uri, String mimeType) {
        if (uri == null) {
            return null;
        }

        Item albumItem = null;
        if (MediaType.checkImageMimeType(mimeType)) {
            albumItem = new Image(uri) {
            };
        } else if (MediaType.checkVideoMimeType(mimeType)) {
            albumItem = new Video(uri);
        }

        if (albumItem != null) {
            albumItem.setFilePath(uri.getPath());
            albumItem.setUri(uri);

        }
        return albumItem;
    }
    public void setDuration(String duration) {  }

    public String getDuration() {  return "";}

}
