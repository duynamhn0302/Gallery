package com.example.gallery.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.Nullable;

import com.example.gallery.adapter.PeopleAdapter;
import com.example.gallery.model.Album;
import com.example.gallery.model.Image;
import com.example.gallery.R;
import com.example.gallery.model.Item;

import java.util.ArrayList;

public class Fragment3 extends androidx.fragment.app.Fragment {

    ArrayList<Album> albums = new ArrayList<>();
    private ArrayList<Item> items = new ArrayList<>();
    PeopleAdapter adapter;
    GridView gridView;
    public Fragment3() {
        // Required empty public constructor
    }

    public Fragment3(ArrayList<Item> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        albums.clear();
        adapter = new PeopleAdapter(albums, getContext());
        gridView = view.findViewById(R.id.gridView);
        gridView.setAdapter(adapter);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment3, container, false);
    }

}