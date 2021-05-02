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

import org.apache.log4j.chainsaw.Main;

import java.util.ArrayList;

public class AlbumDetailAdapter extends BaseAdapter {
    static public ArrayList<DateAdapter> list = new ArrayList<>();
    private Context context;
    static public  Integer delMode = 0;
    static public Integer countCheck = 0;
    static final public int small = 5;
    static final public int medium = 4;
    static final public int large = 3;
    public int numCol = 4;
    static public int cellHeight = 0;
    public AlbumDetailAdapter(ArrayList<DateAdapter> list, Context context, int numCol){
        this.numCol = numCol;
        this.list = list;
        this.context = context;
    }
    static public void checkAll() {
        for(Item item : MainActivity.items)
            item.setChecked(true);
        countCheck = MainActivity.items.size();
        MainActivity.checkAllFlag = true;
        MainActivity.unCheckAllFlag = false;

    }
    static public void unCheckAll(){
        for(Item item : MainActivity.items)
            item.setChecked(false);
        countCheck = 0;
        MainActivity.checkAllFlag = false;
        MainActivity.unCheckAllFlag = true;
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
        RecyclerView recyclerView = albumDetailView.findViewById(R.id.recyclerView);
        if(MainActivity.hideDate)
            date.setVisibility(View.INVISIBLE);
        date.setText(list.get(position).getDate());
        recyclerView.setAdapter(list.get(position));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, numCol);
        recyclerView.setLayoutManager(gridLayoutManager);
        int height = parent.getMeasuredHeight();
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int numRows = list.get(position).getImages().size() / numCol +  1;
                if ( numRows > 10){
                    params.height = numRows * cellHeight;
                    recyclerView.setLayoutParams(params);
                }
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        return albumDetailView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


}
