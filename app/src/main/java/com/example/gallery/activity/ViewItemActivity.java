package com.example.gallery.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.gallery.R;
import com.example.gallery.model.Item;
import com.github.chrisbanes.photoview.PhotoView;

public class ViewItemActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Item item = (Item) getIntent().getSerializableExtra("item");
        PhotoView imageView = (PhotoView)findViewById(R.id.image);
        imageView.setImageResource(item.getDrawableVal());
        ImageButton edit = findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewItemActivity.this, EditItemActivity.class);
                intent.putExtra("edit", item);
                startActivity(intent);
            }
        });
        ImageButton info = findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewItemActivity.this, InfoItemActivity.class);
                intent.putExtra("info", item);
                startActivity(intent);
            }
        });

    }
}
