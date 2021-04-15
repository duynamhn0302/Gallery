package com.example.gallery.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.adapter.ScreenSlidePagerAdapter;
import com.example.gallery.model.Item;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

public class ViewItemActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    Item item = new Item();
    ArrayList<Item> items;
    ScreenSlidePagerAdapter adapter;

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
        viewPager.setCurrentItem(GalleryActivity.items.indexOf(item), false);
        ImageButton edit = findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewItemActivity.this, EditItemActivity.class);
                intent.putExtra("edit", items.get(viewPager.getCurrentItem()));
                startActivity(intent);
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
    }
    private ScreenSlidePagerAdapter createScreenSlideAdapter() {
        items = GalleryActivity.items;
        ScreenSlidePagerAdapter adapter = new ScreenSlidePagerAdapter(this, items, item);
        return adapter;
    }
}
