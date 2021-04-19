package com.example.gallery.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.gallery.BuildConfig;
import com.example.gallery.R;
import com.example.gallery.adapter.ScreenSlidePagerAdapter;
import com.example.gallery.model.Image;

import java.io.File;
import java.util.ArrayList;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.example.gallery.model.Item;

public class ViewItemActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    Item item;
    ArrayList<Item> items;
    private ImageView imageView;
    ScreenSlidePagerAdapter adapter;
    private final int  DS_PHOTO_EDITOR_REQUEST_CODE = 555;
    public static final String OUTPUT_PHOTO_DIRECTORY = "gallery_edit";
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
                Intent dsPhotoEditorIntent = new Intent(ViewItemActivity.this, DsPhotoEditorActivity.class);
                Uri uri = Uri.fromFile(new File(item.getFilePath()));
                System.out.println(uri.getPath());
                dsPhotoEditorIntent.setData(uri);

                dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, OUTPUT_PHOTO_DIRECTORY);

                startActivityForResult(dsPhotoEditorIntent, DS_PHOTO_EDITOR_REQUEST_CODE);

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
                    if (item.isImage())
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case DS_PHOTO_EDITOR_REQUEST_CODE:
                    Uri outputUri = data.getData();
                    Toast.makeText(this, "Photo saved in " + OUTPUT_PHOTO_DIRECTORY + " folder.", Toast.LENGTH_LONG).show();
                    addPic(outputUri.getPath());
                    break;
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
