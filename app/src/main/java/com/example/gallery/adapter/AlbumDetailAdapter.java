package com.example.gallery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;

import java.util.ArrayList;

public class AlbumDetailAdapter extends RecyclerView.Adapter{
    private ArrayList<DateAdapter> list = new ArrayList<>();
    private Context context;
    public AlbumDetailAdapter(ArrayList<DateAdapter> list, Context context){
        this.list = list;
        this.context = context;
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
        viewHolder.gridView.setAdapter(list.get(position));
        viewHolder.gridView.setVerticalScrollBarEnabled(false);
        setGridViewHeightBasedOnChildren(viewHolder.gridView, 4);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    private class ViewHolder extends RecyclerView.ViewHolder{
        TextView date;
        GridView gridView;
        public ViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            gridView = itemView.findViewById(R.id.gridView);

        }
    }
    public void setGridViewHeightBasedOnChildren(GridView gridView, int columns) {
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int items = listAdapter.getCount();
        int rows = 0;

        View listItem = listAdapter.getView(0, null, gridView);
        listItem.measure(0, 0);
        totalHeight = 305;//listItem.getMeasuredHeight();

        float x = 1;
        if( items > columns ){
            x = items/columns;
            rows = (int) (x + 1);
            totalHeight *= rows;
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);

    }
}
