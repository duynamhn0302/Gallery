package com.example.gallery.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.model.Album;
import com.example.gallery.adapter.AlbumAdapter;
import com.example.gallery.model.Item;
import com.example.gallery.R;

import java.util.ArrayList;

public class Fragment2 extends androidx.fragment.app.Fragment {

    ArrayList<Album> albums = new ArrayList<>();
    private ArrayList<Item> items = new ArrayList<>();
    AlbumAdapter adapter;
    RecyclerView recyclerView;
    public Fragment2() {
        // Required empty public constructor
    }

    public Fragment2(ArrayList<Item> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        albums.clear();
        adapter = new AlbumAdapter(albums, getContext());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView = (RecyclerView) getView().findViewById(R.id.albums);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment2, container, false);
    }

}