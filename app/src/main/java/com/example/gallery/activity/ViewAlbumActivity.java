package com.example.gallery.activity;


import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.adapter.AlbumDetailAdapter;
import com.example.gallery.adapter.DateAdapter;
import com.example.gallery.model.Album;
import com.example.gallery.model.Image;
import com.example.gallery.R;
import com.example.gallery.model.Item;

import java.util.ArrayList;

public class ViewAlbumActivity extends AppCompatActivity {

    Album album;
    AlbumDetailAdapter albumDetailAdapter;
    RecyclerView recyclerView;
    public ViewAlbumActivity() {
        // Required empty public constructor
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        album = (Album) getIntent().getSerializableExtra("album");
        TextView name = findViewById(R.id.albumName);
        name.setText(album.getName());
        ArrayList<Item> items = album.getImages();
        ArrayList<DateAdapter> adapters = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            adapters.add(new DateAdapter("Ngay 1/1/2021", items, this));
        }
        albumDetailAdapter = new AlbumDetailAdapter(adapters, this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.album);
        recyclerView.setAdapter(albumDetailAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);

    }
}