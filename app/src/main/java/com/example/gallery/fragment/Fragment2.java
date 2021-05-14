package com.example.gallery.fragment;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.CopyItemsService;
import com.example.gallery.CopyService;
import com.example.gallery.CopyService2;
import com.example.gallery.activity.ChooseItem;
import com.example.gallery.activity.InfoItemActivity;
import com.example.gallery.activity.MainActivity;
import com.example.gallery.activity.ViewItemActivity;
import com.example.gallery.model.Album;
import com.example.gallery.adapter.AlbumAdapter;
import com.example.gallery.model.Image;
import com.example.gallery.R;
import com.example.gallery.model.Item;
import com.example.gallery.model.Video;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class Fragment2 extends androidx.fragment.app.Fragment {
    static public ArrayList<Integer> l = new ArrayList<>();
    private static final int CHOOSE_ITEM = 323;
    ArrayList<Album> albums = new ArrayList<>();
    AlbumAdapter adapter;
    BroadcastReceiver receiver;
    ListView listView;
    Intent service;
    int c = 0;
    int n = 1;
    ProgressDialog mProgressDialog;
    String nameNewAlbum = "";
    ArrayList<Item> choosenItems = new ArrayList<>();
    public Fragment2() {
        // Required empty public constructor
    }

    public AlbumAdapter getAdapter() {
        return adapter;
    }

    public Fragment2(ArrayList<Album> albums) {
        this.albums.clear();
        this.albums.addAll(albums);
    }

    public void setAlbums(ArrayList<Album> albums) {
        this.albums = albums;
        adapter = new AlbumAdapter(albums, getContext());
        listView.setAdapter(adapter);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter = new AlbumAdapter(albums, getContext());
        listView = (ListView) getView().findViewById(R.id.albums);
        listView.setAdapter(adapter);
        FloatingActionButton fab = getView().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertInputText();
            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment2, container, false);
    }
    void showAlertInputText(){

        TextInputLayout textInputLayout = new TextInputLayout(getContext());
        textInputLayout.setPadding(19,0, 19, 0);
        EditText input = new EditText(getContext());
        textInputLayout.setHint(getString(R.string.enter_album_name));
        textInputLayout.addView(input);
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.create_album_title)
                .setView(textInputLayout)
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        nameNewAlbum = input.getText().toString();
                        Intent intent = new Intent(getContext(), ChooseItem.class);
                        startActivityForResult(intent, CHOOSE_ITEM);
                    }
                })
                .create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED)
            return;
        if (requestCode == CHOOSE_ITEM){
            ContentResolver resolver = getContext().getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/" + nameNewAlbum);
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            String path = String.valueOf(uri);
            File folder = new File(path);
            boolean isCreada = folder.exists();
            if(!isCreada) {
                folder.mkdirs();
            }
            nameNewAlbum = path;
            nameNewAlbum = getRealPathFromURI(uri);
            int index = nameNewAlbum.lastIndexOf("/");
            nameNewAlbum = nameNewAlbum.substring(0, index + 1);
            MainActivity.unregist();
            service = new Intent(getContext(), CopyItemsService.class);
            IntentFilter mainFilter = new IntentFilter("matos.action.GOSERVICE4");
            receiver = new MyMainLocalReceiver();
            getContext().registerReceiver(receiver, mainFilter);
            service.putExtra("path", nameNewAlbum);
            getContext().startService(service);

            n = MainActivity.buffer.size();
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.hide), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mProgressDialog.dismiss();
                }
            });
            mProgressDialog.show();

        }
    }
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    public class MyMainLocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context localContext, Intent callerIntent) {

            int progress = callerIntent.getIntExtra("number", 0); //get the progress
            System.out.println(progress);
            mProgressDialog.setProgress(progress * 100 / n);
            if (progress == n) {
                MainActivity.buffer.clear();
                mProgressDialog.dismiss();
                MainActivity.loadAllFiles();
                getContext().unregisterReceiver(receiver);
                MainActivity.refesh();
                MainActivity.regist();
            }
        }
    }
}