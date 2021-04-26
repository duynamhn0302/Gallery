package com.example.gallery.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.activity.ViewItemActivity;
import com.example.gallery.model.Image;
import com.example.gallery.model.Item;

import java.util.ArrayList;
import java.util.List;

public class HeaderNumberedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_VIEW_TYPE_HEADER = 0;
    private static final int IS_IMAGE = 1;
    private static final int IS_VIDEO = 2;
    private Context context;
    private  View header;
    String date;
    private ArrayList<Item> items = new ArrayList<>();
    public HeaderNumberedAdapter(String date,ArrayList<Item> items, Context context) {
        this.items = items;
        this.date = date;
        this.context = context;
        header = LayoutInflater.from(context).inflate(R.layout.header, null);
    }

    public boolean isHeader(int position) {
        return position == 0;
    }

    public String getDate() {
        return date;
    }
    @Override
    public int getItemViewType(int position) {
        if (isHeader(position))
            return ITEM_VIEW_TYPE_HEADER;

        if(items.get(position).isImage())
            return IS_IMAGE;
        return IS_VIDEO;

    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ITEM_VIEW_TYPE_HEADER)
            return new TextViewHolder(inflater.inflate(R.layout.header, parent, false));
        if (viewType == IS_IMAGE)
            return new ImageViewHolder(inflater.inflate(R.layout.image, parent, false));
        return new VideoViewHolder(inflater.inflate(R.layout.video_item_layout, parent, false));
    }
    private final int heigh = 300;
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof TextViewHolder) {
            ((TextViewHolder)holder).textView.setText("date");
            return;
        }
        if (holder instanceof ImageViewHolder) {
            ImageView imageView = ((ImageViewHolder)holder).imageView;
            Glide.with(context).load(items.get(position).getFilePath()).into(imageView);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Do some work here
                    Intent intent = new Intent((Activity) context, ViewItemActivity.class);
                    intent.putExtra("item", items.get(position));
                    ((Activity) context).startActivity(intent);
                }
            });
        } else {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View convertView = ((VideoViewHolder)holder).video;
            ImageView thumbnail = convertView.findViewById(R.id.thumbnail);
            Glide.with(context).load(items.get(position).getFilePath()).into(thumbnail);
            thumbnail.setLayoutParams(new LinearLayout.LayoutParams(-1, heigh));
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout duration = convertView.findViewById(R.id.duration);
            TextView textView = convertView.findViewById(R.id.text_duration);
            textView.setText(items.get(position).getDuration());
            thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Do some work here
                    Intent intent = new Intent((Activity) context, ViewItemActivity.class);
                    intent.putExtra("item", items.get(position));
                    ((Activity) context).startActivity(intent);
                }
            });
        }

    }


    @Override
    public int getItemCount() {
        return items.size() + 1;
    }
    class TextViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public TextViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.header);
        }
    }
    class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView =  itemView.findViewById(R.id.image);
        }
    }
    class VideoViewHolder extends RecyclerView.ViewHolder {
        public View video;
        public VideoViewHolder(View itemView) {
            super(itemView);
            video =  itemView;
        }
    }

}