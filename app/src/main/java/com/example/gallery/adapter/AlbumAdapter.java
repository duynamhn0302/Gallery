package com.example.gallery.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gallery.activity.PrivateAlbumActivity;
import com.example.gallery.activity.ViewAlbumActivity;
import com.example.gallery.model.Album;
import com.example.gallery.R;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter {
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View albumView = inflater.inflate(R.layout.album, parent, false);
        return new ViewHolder(albumView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Album album = list.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.albumName.setText(album.getName());
        viewHolder.countItems.setText(Integer.toString(album.getCount()));
        Glide.with(context).load(album.getMainItem().getFilePath()).into(viewHolder.item);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent((Activity) context, PrivateAlbumActivity.class);
                intent.putExtra("album", list.get(position));
                ((Activity) context).startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder  {
        public TextView albumName;
        public TextView countItems;
        public ImageView item;
        public View itemView;
        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            item = itemView.findViewById(R.id.item);
            albumName = itemView.findViewById(R.id.albumName);
            countItems = itemView.findViewById(R.id.countItems);

        }

    }
}
