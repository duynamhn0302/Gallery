package com.example.gallery.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.model.Album;
import com.example.gallery.model.Item;

import java.util.ArrayList;

public class ChooseAlbum extends BaseActivity {
    boolean hiddenPrivate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseActivity.changeLanguage(MainActivity.language, this);
        BaseActivity.changeTheme(MainActivity.light, this);
        setContentView(R.layout.activity_choose_album);
        ArrayList<Album> l = new ArrayList<>();
        hiddenPrivate = getIntent().getBooleanExtra("hiddenPrivate", false);
        if (!hiddenPrivate)
            l.add(MainActivity.privateAlbum);
        l.addAll(MainActivity.albums);
        AlbumAdapter adapter = new AlbumAdapter(l, this);
        ListView listView = findViewById(R.id.albums);
        listView.setAdapter(adapter);
    }
    private class AlbumAdapter extends BaseAdapter {
        private ArrayList<Album> list = new ArrayList<>();
        private Context context;
        public AlbumAdapter(ArrayList<Album>  list, Context context) {
            this.list = list;
            this.context = context;
        }
        public void addAlbum(Album album){
            list.add(album);
        }
        @NonNull
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            Album album = list.get(position);
            View albumView = inflater.inflate(R.layout.album, parent, false);
            ImageView item = albumView.findViewById(R.id.image);
            TextView albumName = albumView.findViewById(R.id.albumName);
            TextView countItems = albumView.findViewById(R.id.countItems);
            albumName.setText(album.getName());
            countItems.setText(Integer.toString(album.getCount()));
            if (album.getMainImage() != null && album.getType() != Album.typePrivate)
                Glide.with(context).load(album.getMainImage().getFilePath()).into(item);
            if (album.getType() == Album.typePrivate)
                Glide.with(context).load(R.drawable.ic_baseline_lock_24).into(item);
            albumView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.curAlbum = list.get(position);
                    Intent intent = new Intent();
                    if (!hiddenPrivate)
                        intent.putExtra("id_album_choose", position - 1);
                    else
                        intent.putExtra("id_album_choose", position);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
            return albumView;
        }

    }
}