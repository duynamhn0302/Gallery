package com.example.gallery.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.model.Album;
import com.example.gallery.model.Image;
import com.example.gallery.model.Item;
import com.github.chrisbanes.photoview.PhotoView;


public class IntentReceiver extends AppCompatActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch(id){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = prefs.edit();
        boolean light = prefs.getBoolean("theme", true);
        String language = prefs.getString("language" , "vi");
        BaseActivity.changeLanguage(language, this);
        BaseActivity.changeTheme(light, this);
        setContentView(R.layout.activity_view_item_from_file);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        String mimeType = intent.getType();
        String path = uri.getPath();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        System.out.println(path);
        FrameLayout frameLayout = findViewById(R.id.item);
        LayoutInflater inflater = LayoutInflater.from(this);
        Item albumItem = Item.getInstance(this, uri, mimeType);
        if (albumItem == null) {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        if (albumItem instanceof Image) {
            View view = inflater.inflate(R.layout.image_on_slide, null);
            PhotoView photoView = view.findViewById(R.id.imageOnSlide);
            Glide.with(this).load(uri).into(photoView);
            frameLayout.addView(view);
            LinearLayout l = view.findViewById(R.id.toolBottom);
            l.setVisibility(View.INVISIBLE);
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
            LinearLayout l = view.findViewById(R.id.toolBottom);
            l.setVisibility(View.INVISIBLE);
        }


    }



}
