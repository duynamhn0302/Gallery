package com.example.gallery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class AlbumDetailAdapter extends RecyclerView.Adapter{
    static public ArrayList<DateAdapter> list = new ArrayList<>();
    private Context context;
    static public  Integer delMode = 0;
    static public Integer countCheck = 0;
    public AlbumDetailAdapter(ArrayList<DateAdapter> list, Context context){
        this.list = list;
        this.context = context;
    }
    static public void checkAll() {
        for(Item item : MainActivity.items)
            item.setChecked(true);
        countCheck = MainActivity.items.size();
    }
    static public void unCheckAll(){
        for(Item item : MainActivity.items)
            item.setChecked(false);
        countCheck = 0;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View albumDetailView = inflater.inflate(R.layout.items_by_date, parent, false);
        return new AlbumDetailAdapter.ViewHolder(albumDetailView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.date.setText(list.get(position).getDate());
        viewHolder.recyclerView.setAdapter(list.get(position));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4);
        viewHolder.recyclerView.setLayoutManager(gridLayoutManager);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    private class ViewHolder extends RecyclerView.ViewHolder{
        TextView date;
        RecyclerView recyclerView;
        public ViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            recyclerView = itemView.findViewById(R.id.recyclerView);
        }
    }
    static public void notifyChanged(){
        for(DateAdapter adapter:list)
            adapter.notifyDataSetChanged();
    }
}
