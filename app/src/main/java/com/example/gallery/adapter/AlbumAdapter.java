package com.example.gallery.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gallery.activity.MainActivity;
import com.example.gallery.activity.PrivateAlbumActivity;
import com.example.gallery.activity.ViewAlbumActivity;
import com.example.gallery.model.Album;
import com.example.gallery.R;

import java.util.ArrayList;

public class AlbumAdapter extends BaseAdapter {
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
        if (album.getMainImage() != null)
            Glide.with(context).load(album.getMainImage().getFilePath()).into(item);
        albumView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.curAlbum = list.get(position);
                Intent intent = new Intent((Activity) context, ViewAlbumActivity.class);
                intent.putExtra("album", list.get(position));
                ((Activity) context).startActivity(intent);
            }
        });
        return albumView;
    }


}
