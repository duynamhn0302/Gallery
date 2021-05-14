package com.example.gallery.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.activity.MainActivity;
import com.example.gallery.model.Item;

import java.util.ArrayList;

public class AlbumDetailAdapter extends BaseAdapter {
    public ArrayList<DateAdapter> list = new ArrayList<>();
    private Context context;
    static public  boolean delMode = false;
    static final public int small = 5;
    static final public int medium = 4;
    static final public int large = 3;
    public int numCol = 4;
    RecyclerView recyclerView;
    public AlbumDetailAdapter(ArrayList<DateAdapter> list, Context context, int numCol){
        this.numCol = numCol;
        this.list = list;
        this.context = context;
    }

    public  ArrayList<DateAdapter> getList() {
        return list;
    }

    static public void checkAll() {
        MainActivity.buffer.clear();
        if (MainActivity.mainMode)
            MainActivity.buffer.addAll(MainActivity.items);
        else
            MainActivity.buffer.addAll(MainActivity.curAlbum.getImages());

    }
    static public void unCheckAll(){
        MainActivity.buffer.clear();
    }

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
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("ViewHolder") View albumDetailView = inflater.inflate(R.layout.items_by_date, parent, false);
        TextView date = albumDetailView.findViewById(R.id.date);
        recyclerView = albumDetailView.findViewById(R.id.recyclerView);
        if(MainActivity.hideDate)
            date.setVisibility(View.INVISIBLE);
        date.setText(list.get(position).getDate());
        recyclerView.setAdapter(list.get(position));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, numCol);
        recyclerView.setLayoutManager(gridLayoutManager);

        return albumDetailView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


}
