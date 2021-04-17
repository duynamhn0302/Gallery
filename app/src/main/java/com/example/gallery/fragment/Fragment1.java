package com.example.gallery.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.adapter.AlbumDetailAdapter;
import com.example.gallery.adapter.DateAdapter;
import com.example.gallery.model.Item;

import java.util.ArrayList;

public class Fragment1 extends androidx.fragment.app.Fragment {
    AlbumDetailAdapter albumDetailAdapter;
    RecyclerView recyclerView;
    private ArrayList<GridView> gridViews = new ArrayList<>();
    private ArrayList<Item> items = new ArrayList<>();
    private ArrayList<DateAdapter> adapters = new ArrayList<>();
    public Fragment1() {
        // Required empty public constructor
    }

    public Fragment1(ArrayList<Item> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    public void getAllDateAdapter() {
        String currentDate = items.get(0).getAddedDate();
        adapters.add(new DateAdapter(items.get(0).getAddedDate(), items, getContext()));
        for (Item item : items) {
            if (!item.getAddedDate().equals(currentDate)) {
                currentDate = item.getAddedDate();
                adapters.add(new DateAdapter(item.getAddedDate(), items, getContext()));
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getAllDateAdapter();

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