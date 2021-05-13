package com.example.gallery.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

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
    ListView listView;
    private ArrayList<DateAdapter> adapters = new ArrayList<>();
    int numCol = 4;
    public Fragment1(ArrayList<DateAdapter> adapters, int numCol) {
        this.adapters = adapters;
        this.numCol = numCol;
    }
    public Fragment1() {
        // Required empty public constructor
    }

    public AlbumDetailAdapter getAlbumDetailAdapter() {
        return albumDetailAdapter;
    }

    public void setNumCol(int numCol) {
        this.numCol = numCol;
    }

    public void setAdapters(ArrayList<DateAdapter> adapters) {
        if (adapters == null)
            return;
        this.adapters = adapters;
        albumDetailAdapter = new AlbumDetailAdapter(adapters, getContext(), numCol);
        listView.setAdapter(albumDetailAdapter);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        albumDetailAdapter = new AlbumDetailAdapter(adapters, getContext(), numCol);
        listView = (ListView) getView().findViewById(R.id.album);
        listView.setAdapter(albumDetailAdapter);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment1, container, false);
    }

}