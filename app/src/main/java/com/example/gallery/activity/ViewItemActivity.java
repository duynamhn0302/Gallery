package com.example.gallery.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.gallery.BuildConfig;
import com.example.gallery.R;
import com.example.gallery.adapter.ScreenSlidePagerAdapter;
import com.example.gallery.model.Item;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import iamutkarshtiwari.github.io.ananas.editimage.EditImageActivity;
import iamutkarshtiwari.github.io.ananas.editimage.ImageEditorIntentBuilder;

public class ViewItemActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    Item item = new Item();
    ArrayList<Item> items;
    ScreenSlidePagerAdapter adapter;
    private final int PHOTO_EDITOR_REQUEST_CODE = 231;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        item = (Item) getIntent().getSerializableExtra("item");
        viewPager = findViewById(R.id.imagesSlider);
        adapter = createScreenSlideAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(MainActivity.items.indexOf(item), false);
        ImageButton edit = findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editImageClick(items.get(viewPager.getCurrentItem()));
            }
        });
        ImageButton info = findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewItemActivity.this, InfoItemActivity.class);
                intent.putExtra("info", items.get(viewPager.getCurrentItem()));
                startActivity(intent);
            }
        });

        ImageButton share = findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = items.get(viewPager.getCurrentItem());
                File fileToShare = new File(item.getFilePath());
                if (fileToShare.exists()) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri uri = FileProvider.getUriForFile(ViewItemActivity.this,
                            BuildConfig.APPLICATION_ID + ".provider", fileToShare);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    if (item.getIsImage())
                        intent.setType("image/*");
                    else
                        intent.setType("video/*");
                    startActivity(Intent.createChooser(intent, "Share file using"));
                }
            }
        });
    }

    private ScreenSlidePagerAdapter createScreenSlideAdapter() {
        items = MainActivity.items;
        ScreenSlidePagerAdapter adapter = new ScreenSlidePagerAdapter(this, items, item);
        return adapter;
    }
    private void editImageClick(Item item) {
        try {
            Intent intent = new ImageEditorIntentBuilder(this, item.getFilePath(), newName(item.getFilePath()))
                    .withAddText() // Add the features you need
                    .withPaintFeature()
                    .withFilterFeature()
                    .withRotateFeature()
                    .withCropFeature()
                    .withBrightnessFeature()
                    .withSaturationFeature()
                    .withBeautyFeature()
                    .withStickerFeature()
                    .forcePortrait(true)  // Add this to force portrait mode (It's set to false by default)
                    .setSupportActionBarVisibility(false) // To hide app's default action bar
                    .build();

            EditImageActivity.start(ViewItemActivity.this, intent, PHOTO_EDITOR_REQUEST_CODE);
        } catch (Exception e) {
            Log.e("Demo App", e.getMessage()); // This could throw if either `sourcePath` or `outputPath` is blank or Null
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_EDITOR_REQUEST_CODE) { // same code you used while starting
            String newFilePath = data.getStringExtra(ImageEditorIntentBuilder.OUTPUT_PATH);
            boolean isImageEdit = data.getBooleanExtra(EditImageActivity.IS_IMAGE_EDITED, false);
            System.out.println(newFilePath);
            if (isImageEdit){
                ImageView image = new ImageView(this);
                image.setImageURI(Uri.parse(newFilePath));
                Bitmap bmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
                try{
                    FileOutputStream fout = new FileOutputStream(newFilePath);
                    bmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                    fout.close();
                    addPic(newFilePath);
                    Toast.makeText(getApplicationContext(), "Image save" + newFilePath, Toast.LENGTH_LONG).show();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }


        }
    }

    void addPic(String path){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri uri = Uri.fromFile(f);
        intent.setData(uri);
        sendBroadcast(intent);
    }
    String newName(String name){
        int i = name.lastIndexOf('.');
        return name.substring(0, i) + "edit" + name.substring(i);
    }

}
