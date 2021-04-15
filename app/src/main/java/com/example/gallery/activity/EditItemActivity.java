package com.example.gallery.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.model.Album;
import com.example.gallery.model.Item;
import com.github.chrisbanes.photoview.PhotoView;

public class EditItemActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        
        PhotoView imageView = (PhotoView)findViewById(R.id.image);
        Item item = (Item) getIntent().getSerializableExtra("edit");
        Glide.with(this).load(item.getFilePath()).fitCenter().into(imageView);


    }
}
