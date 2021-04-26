package com.example.gallery.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.activity.MainActivity;
import com.example.gallery.activity.ViewItemActivity;
import com.example.gallery.model.Image;
import com.example.gallery.model.Item;

import java.util.ArrayList;

public class DateAdapter extends RecyclerView.Adapter {
    private String date;
    private ArrayList<Item> items = new ArrayList<>();
    private Context context;
    private final int IMAGE_TYPE = 0;
    private final int VIDEO_TYPE = 1;
    public DateAdapter(String date) {
        this.date = date;
    }

    public DateAdapter(String date, ArrayList<Item> items, Context context) {
        this.date = date;
        this.context = context;
        this.items = items;
    }

    public ArrayList<Item> getImages() {
        return items;
    }

    public void setItems() {
        this.items = items;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }




    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = null;
        if (viewType== IMAGE_TYPE){
            view = inflater.inflate(R.layout.image, parent, false);
            return new DateAdapter.ImageViewHolder(view);
        }
        view = inflater.inflate(R.layout.video_item_layout, parent, false);
        return new DateAdapter.VideoViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (items.get(position).isImage())
        {
            ImageViewHolder imageViewHolder = (ImageViewHolder)holder;

            ImageView imageView = imageViewHolder.imageView;
            Glide.with(context).load(items.get(position).getFilePath()).into(imageView);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Do some work here
                    Intent intent = new Intent((Activity) context, ViewItemActivity.class);
                    intent.putExtra("item", items.get(position));
                    ((Activity) context).startActivityForResult(intent, MainActivity.UPDATED_CODE);
                }
            });
            return;
        }
        VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
        ImageView thumbnail = videoViewHolder.thumnail;
        Glide.with(context).load(items.get(position).getFilePath()).into(thumbnail);
        thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        TextView textView = videoViewHolder.duration;
        textView.setText(items.get(position).getDuration());
        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Do some work here
                Intent intent = new Intent((Activity) context, ViewItemActivity.class);
                intent.putExtra("item", items.get(position));
                ((Activity) context).startActivityForResult(intent, MainActivity.UPDATED_CODE);
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        return Item.isImageFile((items.get(position).getFilePath())) ? IMAGE_TYPE:VIDEO_TYPE;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
        }
    }
    private class VideoViewHolder extends RecyclerView.ViewHolder{
        ImageView thumnail;
        TextView duration;
        public VideoViewHolder(View itemView) {
            super(itemView);
            thumnail = itemView.findViewById(R.id.thumbnail);
            duration = itemView.findViewById(R.id.text_duration);
        }
    }
}
