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

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.activity.ViewItemActivity;
import com.example.gallery.model.Image;
import com.example.gallery.model.Item;

import java.util.ArrayList;

public class DateAdapter extends BaseAdapter {
    private String date;
    private ArrayList<Item> items = new ArrayList<>();
    private Context context;
    private final int heigh = 300;

    public DateAdapter(String date) {
        this.date = date;
    }

    public DateAdapter(String date, ArrayList<Item> items, Context context) {
        this.date = date;
        this.context = context;
        this.items = new ArrayList<>();
        for (Item i : items) {
            if (date.equals(i.getAddedDate())) {
                this.items.add(i);
            }
        }
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

    @Override
    public int getCount() {
        if (items == null)
            return 0;
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (items.get(position).isImage()) {
            ImageView imageView = new ImageView(context);
            Glide.with(context).load(items.get(position).getFilePath()).into(imageView);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new GridView.LayoutParams(-1, heigh));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Do some work here
                    Intent intent = new Intent((Activity) context, ViewItemActivity.class);
                    intent.putExtra("item", items.get(position));
                    ((Activity) context).startActivity(intent);
                }
            });
            return imageView;
        } else {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = (RelativeLayout) layoutInflater.inflate(R.layout.video_item_layout, parent, false);
            convertView.setLayoutParams(new GridView.LayoutParams(-1, heigh));
            ImageView thumbnail = convertView.findViewById(R.id.thumbnail);
            Glide.with(context).load(items.get(position).getFilePath()).into(thumbnail);
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
            return convertView;
        }
    }
}
