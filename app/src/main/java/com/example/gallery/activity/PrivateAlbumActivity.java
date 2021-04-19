package com.example.gallery.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gallery.R;
import com.example.gallery.model.Album;

public class PrivateAlbumActivity extends AppCompatActivity {
    Album album;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_album);
        ImageButton unlock = findViewById(R.id.unlock);
        album = (Album) getIntent().getSerializableExtra("album");
        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrivateAlbumActivity.this, ViewAlbumActivity.class);
                intent.putExtra("album", album);
                startActivity(intent);
            }
        });

    }
}
