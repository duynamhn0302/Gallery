package com.example.gallery.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.model.Image;
import com.example.gallery.model.Item;
import com.github.chrisbanes.photoview.PhotoView;


public class IntentReceiver extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_item_from_file);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        String mimeType = intent.getType();
        String path = uri.getPath();
        System.out.println(path);
        FrameLayout frameLayout = findViewById(R.id.item);
        LayoutInflater inflater = LayoutInflater.from(this);
        Item albumItem = Item.getInstance(this, uri, mimeType);
        if (albumItem == null) {
            Toast.makeText(this, "getString(R.string.error)", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        if (albumItem instanceof Image) {
            View view = inflater.inflate(R.layout.image_on_slide, null);
            PhotoView photoView = view.findViewById(R.id.imageOnSlide);
            Glide.with(this).load(uri).into(photoView);
            frameLayout.addView(view);
        }
        else {
            View view = inflater.inflate(R.layout.video_on_slide, null);
            VideoView videoView = view.findViewById(R.id.videoOnSlide);
            MediaController mediaController = new MediaController(this);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);
            videoView.setVideoURI(albumItem.getUri());
            videoView.seekTo(1);
            frameLayout.addView(view);
        }

    }



}
