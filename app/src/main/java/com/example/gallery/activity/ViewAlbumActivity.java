package com.example.gallery.activity;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.adapter.AlbumDetailAdapter;
import com.example.gallery.adapter.DateAdapter;
import com.example.gallery.model.Album;
import com.example.gallery.R;
import com.example.gallery.model.Item;

import java.io.File;
import java.util.ArrayList;

public class ViewAlbumActivity extends AppCompatActivity {

    static Album album;
    static public AlbumDetailAdapter albumDetailAdapter;
    static ListView listView;
    static public MenuItem checkAll;
    static public MenuItem del;
    static ActionBar actionBar;
    static public Menu menu;
    static public Context context;

    public ViewAlbumActivity() {
        // Required empty public constructor
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.album_menu, menu);
        checkAll = menu.findItem(R.id.checkAll);
        del = menu.findItem(R.id.del);
        hideMenu();
        CheckBox checkBox = (CheckBox)checkAll.getActionView();

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked()){
                    AlbumDetailAdapter.checkAll();
                }
                else {
                    AlbumDetailAdapter.unCheckAll();
                }
                albumDetailAdapter.notifyDataSetChanged();
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch(id){

            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.del:
                if(AlbumDetailAdapter.countCheck == 0)
                    break;
                showDeleteDialog();
                break;
            case R.id.show_hideDate:
                if (MainActivity.hideDate)
                    MainActivity.hideDate = false;
                else
                    MainActivity.hideDate = true;
                MainActivity.prefsEditor.putBoolean("hideDate", MainActivity.hideDate);
                MainActivity.prefsEditor.apply();
                refesh();
                break;
            case R.id.small:
                MainActivity.numCol = AlbumDetailAdapter.small;
                MainActivity.prefsEditor.putInt("numCol", MainActivity.numCol);
                MainActivity.prefsEditor.apply();
                refesh();
                break;
            case R.id.medium:
                MainActivity.numCol = AlbumDetailAdapter.medium;
                MainActivity.prefsEditor.putInt("numCol", MainActivity.numCol);
                MainActivity.prefsEditor.apply();
                refesh();

                break;
            case R.id.large:
                MainActivity.numCol = AlbumDetailAdapter.large;
                MainActivity.prefsEditor.putInt("numCol", MainActivity.numCol);
                MainActivity.prefsEditor.apply();
                refesh();
                break;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }
    static public void showMenu(){
        checkAll.setVisible(true);
        del.setVisible(true);
        menu.setGroupVisible(R.id.group, false);
    }
    static public void hideMenu(){
        checkAll.setVisible(false);
        del.setVisible(false);
        menu.setGroupVisible(R.id.group, true);
    }
    private void deleteItems(){
        AlbumDetailAdapter.countCheck = 0;
        ArrayList<Item> items = album.getImages();
        for(int i = items.size() - 1; i >= 0; i--){
            if (items.get(i).getChecked()){
                File file = new File(items.get(i).getFilePath());
                if(!file.delete()){
                    System.out.println("K xoa dc");
                }
                MainActivity.originalItems.remove(items.get(i));
                items.remove(i);
            }
        }
        refesh();
        hideMenu();
        AlbumDetailAdapter.delMode = (AlbumDetailAdapter.delMode + 1) % 2;
    }
    @Override
    public void onBackPressed() {
        ArrayList<Item> items = album.getImages();
        if(AlbumDetailAdapter.delMode == 1){
            for(Item item:items)
                item.setChecked(false);
            AlbumDetailAdapter.countCheck = 0;
            hideMenu();
            AlbumDetailAdapter.delMode = (AlbumDetailAdapter.delMode + 1) % 2;
            AlbumDetailAdapter.unCheckAll();
            albumDetailAdapter.notifyDataSetChanged();
            return;
        }
        MainActivity.mainMode = true;
        MainActivity.items = MainActivity.originalItems;
        MainActivity.clearDelMode();
        MainActivity.refesh();
        super.onBackPressed();

    }
    public void showDeleteDialog() {
        new AlertDialog.Builder(this, getTheme().hashCode())
                .setTitle(R.string.delete_item + "?")
                .setNegativeButton(getString(R.string.no), null)
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteItems();
                    }
                })
                .create().show();
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.mainMode = false;
        MainActivity.clearDelMode();
        setContentView(R.layout.activity_view_album);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar = getSupportActionBar();
        album = (Album) getIntent().getSerializableExtra("album");
        TextView name = findViewById(R.id.albumName);
        name.setText(album.getName());
        MainActivity.items = album.getImages();
        context = this;
        ArrayList<DateAdapter> adapters = MainActivity.getAllDateAdapter(MainActivity.items, this);
        albumDetailAdapter = new AlbumDetailAdapter(adapters, context, MainActivity.numCol);
        listView = (ListView) findViewById(R.id.album);
        listView.setAdapter(albumDetailAdapter);

    }
    static public void refesh(){
        ArrayList<DateAdapter> adapters = MainActivity.getAllDateAdapter(MainActivity.items, context);
        albumDetailAdapter = new AlbumDetailAdapter(adapters, context, MainActivity.numCol);
        listView.setAdapter(albumDetailAdapter);

    }
}