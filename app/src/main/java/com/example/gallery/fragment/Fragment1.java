package com.example.gallery.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.adapter.AlbumAdapter;
import com.example.gallery.adapter.AlbumDetailAdapter;
import com.example.gallery.adapter.DateAdapter;
import com.example.gallery.model.Album;
import com.example.gallery.model.Item;

import java.util.ArrayList;

public class Fragment1 extends androidx.fragment.app.Fragment {
    AlbumDetailAdapter albumDetailAdapter;
    RecyclerView recyclerView;
    private ArrayList<GridView> gridViews = new ArrayList<>();
    public Fragment1() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ArrayList<Item> items = new ArrayList<>();
        for(int i = 0; i < 9; i++){
            items.add(new Item(R.drawable.abc));
        }
        ArrayList<DateAdapter> adapters = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            adapters.add(new DateAdapter("Ngay 1/1/2021",items, getContext()));
        }
        albumDetailAdapter = new AlbumDetailAdapter(adapters, getContext());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView = (RecyclerView) getView().findViewById(R.id.album);
        recyclerView.setAdapter(albumDetailAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment1, container, false);
    }

}