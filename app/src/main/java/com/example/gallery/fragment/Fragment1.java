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
import com.example.gallery.model.Image;
import com.example.gallery.model.Item;

import java.util.ArrayList;

public class Fragment1 extends androidx.fragment.app.Fragment {
    AlbumDetailAdapter albumDetailAdapter;
    RecyclerView recyclerView;
    private ArrayList<DateAdapter> adapters = new ArrayList<>();


    public Fragment1(ArrayList<DateAdapter> adapters) {
        this.adapters = adapters;
    }


    public AlbumDetailAdapter getAlbumDetailAdapter() {
        return albumDetailAdapter;
    }

    public void setAdapters(ArrayList<DateAdapter> adapters) {
        this.adapters = adapters;
        albumDetailAdapter = new AlbumDetailAdapter(adapters, getContext());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(albumDetailAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

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