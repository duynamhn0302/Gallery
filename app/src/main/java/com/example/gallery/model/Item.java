package com.example.gallery.model;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.loader.content.CursorLoader;

import com.example.gallery.activity.MainActivity;
import com.example.gallery.activity.ViewAlbumActivity;
import com.example.gallery.adapter.DateAdapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public abstract class Item implements Serializable {

    protected String filePath, addedDate, location;
    long id;
    protected Uri uri;
    private int headerId;
    protected boolean isImage;
    protected boolean isLoved = false;
    Item(){

    }
    static public Item newItem(String filePath, Context context){
        Item item = null;
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DURATION,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
        };

        // Return only video and image metadata.
        String selection = MediaStore.Files.FileColumns.DATA + " = " + "'" + filePath + "'";

        Uri queryUri = MediaStore.Files.getContentUri("external");

        CursorLoader cursorLoader = new CursorLoader(
                context,
                queryUri,
                projection,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC LIMIT 1" // Sort order.
        );
        Cursor cursor = cursorLoader.loadInBackground();
        if (cursor.moveToNext()) {
            String absolutePathOfFile = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            Long longDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED));
            Long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
            Long durationNum = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION));
            String addedDate = DateFormat.format("dd/MM/yyyy", new Date(longDate * 1000)).toString();
            String albumName = cursor.getString((cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)));

            ExifInterface exif;
            try{

                exif = new ExifInterface(absolutePathOfFile);
                float[] latLng = new float[2];
                exif.getLatLong(latLng);

                Geocoder geocoder = new Geocoder(context);
                List<Address> addresses = geocoder.getFromLocation(latLng[0], latLng[1], 1);

                boolean isLoved = MainActivity.isInLoveList(absolutePathOfFile, MainActivity.listLove);

                if (Image.isImageFile(absolutePathOfFile))
                    item = new Image(id, absolutePathOfFile,  addedDate, false);
                if (Image.isVideoFile(absolutePathOfFile)) {
                    item = new Video(id, absolutePathOfFile,  addedDate, Image.convertToDuration(durationNum), durationNum, false);
                }
            }
            catch (Exception ex){
                System.out.println(absolutePathOfFile);
            }
            Album album = MainActivity.existsAlbum(albumName);
            if (album == null){
                album = new Album(albumName);
                MainActivity.albums.add(album);
            }
            album.addHead(item);
        }
        cursor.close();

        return item;
    }
    public  void delete(Context context, boolean privateFolder){
        File f = new File(getFilePath());
        f.delete();
        MainActivity.deleteChange = true;
        if (privateFolder){
            context.getContentResolver().delete(MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                    MediaStore.Files.FileColumns.DATA + "=?", new String[]{getFilePath()});
        }
        else
            context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Files.FileColumns.DATA + "=?", new String[]{getFilePath()});
        clearInAlbum();
        notifyDelete();
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
    public long getId() {
        return id;
    }
    void notifyDelete(){

        if (MainActivity.items.contains(this))
            MainActivity.items.remove(this);
        ArrayList<DateAdapter> adapters = MainActivity.fragment1.getAlbumDetailAdapter().getList();
        for(DateAdapter adapter:adapters){
            if (adapter.getImages().contains(this)){
                adapter.getImages().remove(this);
            }
            adapter.notifyDataSetChanged();
        }
        MainActivity.fragment2.getAdapter().notifyDataSetChanged();
        if(MainActivity.mainMode) return;
        ArrayList<DateAdapter> adapters2 = ViewAlbumActivity.albumDetailAdapter.getList();
        for(DateAdapter adapter:adapters2){
            if (adapter.getImages().contains(this)){
                adapter.getImages().remove(this);
            }
            adapter.notifyDataSetChanged();
        }

    }
    public void setLoved(boolean loved) {
        isLoved = loved;
    }

    public boolean isLoved() {
        return isLoved;
    }


    public void setId(long id) {
        this.id = id;
    }

    public int getHeaderId() {
        return headerId;
    }

    public void setHeaderId(int headerId) {
        this.headerId = headerId;
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
    // check if file were image or not
    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    // check if file were video or not
    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }
    public static String convertToDuration(Long durationNumber) {
        String duration = String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(durationNumber),
                TimeUnit.MILLISECONDS.toSeconds(durationNumber) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationNumber))
        );
        return duration;
    }
    static public String nameCopyOf(String oldPath){
        int i = oldPath.lastIndexOf('.');
        return oldPath.substring(0, i) + "Copy" + oldPath.substring(i);
    }


    long getDurationLong(){
        return 0;
    }
    static String getNameCopyOf(String filePath){
        int i = filePath.lastIndexOf('.');
        return filePath.substring(0, i) + "Copy" +  filePath.substring(i);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String copyTo(Album album, Context context, String volume){
        String name = "";
        try {
            name = createCopyOf(album, context, volume);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name;
    }
     @RequiresApi(api = Build.VERSION_CODES.O)
     public String copy(Context context, String volume){

        Album album = Album.getAlbumOf(this, MainActivity.albums);
        if (album == null)
            return "";
        try {
            return createCopyOf(album, context, volume);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String createCopyOf(Album album, Context context, String volume) throws IOException {
        Item old = this;
        Random r = new Random();
        int i1 = r.nextInt();
        String newItemName = Integer.toString(i1) + "IMAGE_"+ new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        if (volume == "internal"){
            String path = album.getPath() + newItemName ;
            if (old.isImage){
                path += ".jpg";
            }
            else{
                path += ".mp4";
            }
            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(old.getFilePath());
                os = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } finally {
                is.close();
                os.close();
            }
        }
        else
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();

            contentValues.put(MediaStore.MediaColumns.DURATION, getDurationLong());
            String path = album.getPath() + newItemName ;
            if (old.isImage){
                path += ".jpg";
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            }
            else{
                path += ".mp4";
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            }
            contentValues.put(MediaStore.MediaColumns.DATA,  path);
            Uri uri = MediaStore.Files.getContentUri("external");
            Uri imageUri = resolver.insert(uri, contentValues);
            if (imageUri == null)
                return null;
            OutputStream out = resolver.openOutputStream(Objects.requireNonNull(imageUri));
            long x = Files.copy(new File(old.getFilePath()).toPath(), out);
        }
        return newItemName;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String moveTo(Album album, Context context, String volume){
        String name = "";
        try {
            name = createCopyOf(album, context, volume);
            delete(context, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name;
    }
    void clearInAlbum(){
        if (MainActivity.items.contains(this))
            MainActivity.items.remove(this);


        for(Album album : MainActivity.albums)
            if(album.getImages().contains(this)) {
                album.getImages().remove(this);
                break;
            }
        for(String path : MainActivity.listLove)
            if(getFilePath().equals(path)) {
                MainActivity.listLove.remove(path);
                break;
            }
        if(MainActivity.loveAlbum.getImages().contains(this)) {
            MainActivity.loveAlbum.getImages().remove(this);
        }
        if(MainActivity.privateAlbum.getImages().contains(this)) {
            MainActivity.privateAlbum.getImages().remove(this);
        }
    }
}
