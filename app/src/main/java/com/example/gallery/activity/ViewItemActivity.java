package com.example.gallery.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.os.ResultReceiver;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.content.CursorLoader;
import androidx.viewpager2.widget.ViewPager2;

import com.example.gallery.CopyItemsService;
import com.example.gallery.CopyService;
import com.example.gallery.MoveItemsService;
import com.example.gallery.MoveService;
import com.example.gallery.R;
import com.example.gallery.adapter.AlbumDetailAdapter;
import com.example.gallery.adapter.ScreenSlidePagerAdapter;
import com.example.gallery.model.Album;
import com.example.gallery.model.Image;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import com.example.gallery.model.Item;
import com.example.gallery.model.UriUtils;
import com.example.gallery.model.Video;

import org.jetbrains.annotations.NotNull;

import ly.img.android.pesdk.backend.model.EditorSDKResult;
import ly.img.android.pesdk.backend.model.state.manager.SettingsList;
import ly.img.android.pesdk.ui.activity.ImgLyIntent;
import ly.img.android.serializer._3.IMGLYFileWriter;


public class ViewItemActivity extends BaseActivity {
    static final public int PESDK_RESULT = 1;
    private static final int CHOOSE_ALBUM = 12345;
    int n = 1;
    ProgressDialog mProgressDialog;
    static public ViewPager2 viewPager;
    static Item item;
    static public ScreenSlidePagerAdapter adapter;
    static public boolean change = false;
    MenuItem copy;
    MenuItem move;
    MenuItem wallpaper;
    MenuItem slideshow;
    Intent service;
    BroadcastReceiver receiver;
    static ArrayList<Item> items = new ArrayList<>();
    @Override
    public void onBackPressed() {
        int res = change?RESULT_OK:RESULT_CANCELED;
        setResult(RESULT_OK);
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        copy = menu.findItem(R.id.copy);
        wallpaper = menu.findItem(R.id.wallpaper);
        slideshow = menu.findItem(R.id.slideshow);
        move = menu.findItem(R.id.move);
        if (MainActivity.curAlbum != null && MainActivity.curAlbum.getType() == Album.typePrivate){
            copy.setVisible(false);
            move.setTitle(getString(R.string.unlock_string));
        }
        if (!item.isImage())
            wallpaper.setVisible(false);
        else
            wallpaper.setVisible(true);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (wallpaper == null)
                    return;
                if (!adapter.items.get(position).isImage())
                    wallpaper.setVisible(false);
                else
                    wallpaper.setVisible(true);

            }
        });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch(id){

            case R.id.wallpaper:

                Item curr = items.get(viewPager.getCurrentItem());
                Bitmap bitmap = BitmapFactory.decodeFile(curr.getFilePath());
                WallpaperManager manager = WallpaperManager.getInstance(getApplicationContext());
                try{
                    manager.setBitmap(bitmap);
                   // Toast.makeText(this, "Wallpaper set!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                  //  Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.slideshow:
                Intent intent = new Intent(ViewItemActivity.this, SlideShow.class);
                intent.putExtra("number", viewPager.getCurrentItem());
                startActivity(intent);
                break;
            case R.id.copy:
                MainActivity.unregist();
                IntentFilter mainFilter = new IntentFilter("matos.action.GOSERVICE3");
                receiver = new MyMainLocalReceiver();
                registerReceiver(receiver, mainFilter);
                service = new Intent(this, CopyService.class);
                startService(service);
                break;
            case R.id.move:
                Intent intent1 = new Intent(this, ChooseAlbum.class);
                if (MainActivity.curAlbum != null && MainActivity.curAlbum.getType() == Album.typePrivate)
                    intent1.putExtra("hiddenPrivate", true);
                startActivityForResult(intent1, CHOOSE_ALBUM);
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }
    static Context context;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseActivity.changeLanguage(MainActivity.language, this);
        BaseActivity.changeTheme(MainActivity.light, this);
        setContentView(R.layout.activity_item);
        context = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        item = (Item) getIntent().getSerializableExtra("item");
        viewPager = findViewById(R.id.imagesSlider);
        adapter = createScreenSlideAdapter();
        viewPager.setAdapter(adapter);

        if (MainActivity.mainMode)
            viewPager.setCurrentItem(MainActivity.items.indexOf(item), false);
        else
            viewPager.setCurrentItem(MainActivity.curAlbum.getImages().indexOf(item), false);


    }

     static public ScreenSlidePagerAdapter createScreenSlideAdapter() {
        if (MainActivity.mainMode){
            items = MainActivity.items;
        }
        else
            items = MainActivity.curAlbum.getImages();
        ScreenSlidePagerAdapter adapter = new ScreenSlidePagerAdapter((FragmentActivity) context,  items, item);
        return adapter;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED)
            return;
            if (resultCode == RESULT_OK ){
                if(requestCode == PESDK_RESULT) {

                    EditorSDKResult dataa = new EditorSDKResult(data);

                    dataa.notifyGallery(EditorSDKResult.UPDATE_RESULT & EditorSDKResult.UPDATE_SOURCE);
                    Uri uriSource = dataa.getSourceUri();
                    Uri res = dataa.getResultUri();
                    Log.i("PESDK", "Source image is located here " + uriSource);
                    Log.i("PESDK", "Result image is located here " + res);

                    // TODO: Do something with the result image

                    // OPTIONAL: read the latest state to save it as a serialisation
                    SettingsList lastState = dataa.getSettingsList();
                    File newFile = new File(
                            Environment.getExternalStorageDirectory(),
                            "serialisationReadyToReadWithPESDKFileReader.json"
                    );
                    try {

                        new IMGLYFileWriter(lastState).writeJson(newFile);
                    } catch (Exception e) { e.printStackTrace(); }

                    item = newItem();
                    MainActivity.notifyAddNewFile(item);
                    ViewItemActivity.adapter.notifyDataSetChanged();
                    ViewItemActivity.viewPager.setAdapter(ViewItemActivity.adapter);
                    ViewItemActivity.viewPager.setCurrentItem(0);
                } else if (resultCode == RESULT_CANCELED && requestCode == PESDK_RESULT) {
                    // Editor was canceled
                    EditorSDKResult dataa = new EditorSDKResult(data);

                    Uri sourceURI = dataa.getSourceUri();
                    // TODO: Do something...
                }
            }
            if (requestCode == CHOOSE_ALBUM)
            {
                int i = data.getIntExtra("id_album_choose", -2);
                if (i < -1)
                    return;
                MainActivity.unregist();

                IntentFilter mainFilter;
                service = new Intent(this, MoveItemsService.class);
                mainFilter = new IntentFilter("matos.action.GOSERVICE5");
                receiver = new MyMainLocalReceiver2();

                registerReceiver(receiver, mainFilter);
                String nameNewAlbum = "";
                if (i != -1)
                    nameNewAlbum = MainActivity.albums.get(i).getPath();
                else
                    nameNewAlbum = MainActivity.privateAlbum.getPath();
                Item curr = null;
                if(MainActivity.mainMode){

                    curr = MainActivity.items.get(ViewItemActivity.viewPager.getCurrentItem());
                }
                else {
                    curr = adapter.items.get(ViewItemActivity.viewPager.getCurrentItem());
                }
                if (curr == null)
                    return;
                MainActivity.buffer.add(curr);
                n = MainActivity.buffer.size();
                service.putExtra("path", nameNewAlbum);
                startService(service);


                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getString(R.string.loading));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.hide), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressDialog.dismiss();
                        finish();
                    }
                });
                mProgressDialog.show();
            }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        finish();
        startActivity(getIntent());
    }
    private Item newItem() {
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DURATION,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
        };

        // Return only video and image metadata.
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        Uri queryUri = MediaStore.Files.getContentUri("external");

        CursorLoader cursorLoader = new CursorLoader(
                this,
                queryUri,
                projection,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC LIMIT 1" // Sort order.
        );

        Cursor cursor = cursorLoader.loadInBackground();
        Item item = null;
        if (cursor.moveToNext()) {
            String absolutePathOfFile = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            Long longDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED));
            Long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
            Long durationNum = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION));
            String addedDate = DateFormat.format("dd/MM/yyyy", new Date(longDate * 1000)).toString();
            String albumName = cursor.getString((cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)));

            ExifInterface exif;
            try {

                exif = new ExifInterface(absolutePathOfFile);
                float[] latLng = new float[2];
                exif.getLatLong(latLng);

                if (Image.isImageFile(absolutePathOfFile))
                    item = new Image(id, absolutePathOfFile, addedDate, false);
                if (Image.isVideoFile(absolutePathOfFile)) {
                    item = new Video(id, absolutePathOfFile, addedDate, Image.convertToDuration(durationNum), durationNum, false);
                }
                Album album = MainActivity.existsAlbum(albumName);
                if (album == null) {
                    album = new Album(albumName);
                    MainActivity.albums.add(album);
                }
                album.addHead(item);
            } catch (Exception ex) {
                System.out.println(absolutePathOfFile);
            }
        }
        cursor.close();
        return item;
    }
    class MyMainLocalReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context localContext, Intent callerIntent) {
            System.out.println("Da nhan");

             String name = callerIntent.getStringExtra("service3Data");
             Log.e ("MAIN>>>", "Data received from Service3: " + name);
             Item item = newItem();
             MainActivity.notifyAddNewFile(item);
             MainActivity.refesh();
             ViewAlbumActivity.refesh();
             MainActivity.regist();
             if (receiver != null)
                 unregisterReceiver(receiver);
         }
     }
    class MyMainLocalReceiver2 extends BroadcastReceiver {
        @Override
        public void onReceive(Context localContext, Intent callerIntent) {

            int progress = callerIntent.getIntExtra("number", 0); //get the progress
            System.out.println(progress);
            mProgressDialog.setProgress(progress * 100 / n);
            if (progress == n) {
                for(Item item: MainActivity.buffer)
                    item.delete(ViewItemActivity.this, MainActivity.curAlbum.getType() == Album.typePrivate);
                Item item = newItem();
                MainActivity.loadAllFiles();
                MainActivity.refesh();
                ViewAlbumActivity.refesh();
                MainActivity.regist();
                if (receiver != null)
                    unregisterReceiver(receiver);
                MainActivity.buffer.clear();
                mProgressDialog.dismiss();
                finish();
            }

        }

    }

}
