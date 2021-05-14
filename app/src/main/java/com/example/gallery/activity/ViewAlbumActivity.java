package com.example.gallery.activity;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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

import com.example.gallery.CopyItemsService;
import com.example.gallery.MoveItemsService;
import com.example.gallery.MoveService;
import com.example.gallery.adapter.AlbumDetailAdapter;
import com.example.gallery.adapter.DateAdapter;
import com.example.gallery.fragment.Fragment2;
import com.example.gallery.model.Album;
import com.example.gallery.R;
import com.example.gallery.model.Item;

import java.io.File;
import java.util.ArrayList;

import ly.img.android.pesdk.backend.model.EditorSDKResult;
import ly.img.android.pesdk.backend.model.state.manager.SettingsList;
import ly.img.android.serializer._3.IMGLYFileWriter;

public class ViewAlbumActivity extends BaseActivity {

    private static final int PRIVATE_ALBUM = 28;
    private static final int CREATE_PASS = 34;
    private static final int CHOOSE_ALBUM = 4242;
    private static final int CHOOSE_ALBUM_TO_MOVE =786 ;
    private static final int CHECK_PASS = 314;
    private static final int CHANGE_PASS = 643;
    static public AlbumDetailAdapter albumDetailAdapter;
    static ListView listView;
    static public MenuItem checkAll;
    static public MenuItem del;
    static public MenuItem unlock;
    static public MenuItem addTo;
    static public MenuItem hideDate;
    static public String name = "";
    static public MenuItem delAlbum;
    static public MenuItem changepass;
    static ActionBar actionBar;
    static public Menu menu;
    boolean unlocked = false;
    static public Context context;
    Intent service;
    int c = 0;

    BroadcastReceiver receiver;
    int n = 1;
    ProgressDialog mProgressDialog;
    static public ArrayList<DateAdapter> adapters;
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
        unlock = menu.findItem(R.id.unlock);
        hideDate = menu.findItem(R.id.show_hideDate);
        addTo = menu.findItem(R.id.addTo);
        delAlbum = menu.findItem(R.id.delAlbum);
        changepass = menu.findItem(R.id.changepass);
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
                if(MainActivity.buffer.size() == 0)
                    break;
                showDeleteDialog();
                break;
            case R.id.unlock:
                Intent intent1 = new Intent(this, ChooseAlbum.class);
                intent1.putExtra("hiddenPrivate", true);
                startActivityForResult(intent1, CHOOSE_ALBUM_TO_MOVE);
                break;
            case R.id.addTo:
                if(MainActivity.buffer.size() == 0)
                    break;
                showCopyOrMove();
                break;
            case R.id.delAlbum:
                showDeleteAlbumDialog();
                break;
            case R.id.changepass:
                Intent i = new Intent(this, PrivateAlbumActivity.class);
                startActivityForResult(i, CHECK_PASS);
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
                MainActivity.prefsEditor.putInt("numCol", MainActivity.numCol + MainActivity.padding);
                MainActivity.prefsEditor.apply();
                refesh();
                break;
            case R.id.medium:
                MainActivity.numCol = AlbumDetailAdapter.medium;
                MainActivity.prefsEditor.putInt("numCol", MainActivity.numCol + MainActivity.padding);
                MainActivity.prefsEditor.apply();
                refesh();
                break;
            case R.id.large:
                MainActivity.numCol = AlbumDetailAdapter.large;
                MainActivity.prefsEditor.putInt("numCol", MainActivity.numCol + MainActivity.padding);
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
        if (MainActivity.curAlbum.getType() == Album.typePrivate){
            hideDate.setVisible(false);
            addTo.setVisible(false);
        }
        else{
            addTo.setVisible(true);
            hideDate.setVisible(false);
        }
        if (MainActivity.curAlbum.getType() == Album.typePrivate){
            unlock.setVisible(true);
        }
        else
            unlock.setVisible(false);
        menu.setGroupVisible(R.id.group, false);

    }
    static public void hideMenu(){
        addTo.setVisible(false);
        checkAll.setVisible(false);
        del.setVisible(false);
        unlock.setVisible(false);
        menu.setGroupVisible(R.id.group, true);
        if (MainActivity.curAlbum.getType() == Album.typePrivate){
            hideDate.setVisible(false);
        }
        else{
            hideDate.setVisible(true);
        }
        if (MainActivity.curAlbum.getType() == Album.typeLove || MainActivity.curAlbum.getType() == Album.typePrivate)
            delAlbum.setVisible(false);
        else
            delAlbum.setVisible(true);
        if (MainActivity.curAlbum.getType() == Album.typePrivate)
            changepass.setVisible(true);
        else
            changepass.setVisible(false);
    }
    private void deleteAlbum(){
        MainActivity.unregist();
        MainActivity.buffer.addAll(MainActivity.curAlbum.getImages());
        for(Item item : MainActivity.buffer){
            item.delete(context, MainActivity.curAlbum.getType() == Album.typePrivate);
        }
        MainActivity.loadAllFiles();
        MainActivity.refesh();
        finish();

    }
    private void deleteItems(){
        MainActivity.unregist();
        for(Item item : MainActivity.buffer){
            item.delete(context, MainActivity.curAlbum.getType() == Album.typePrivate);
        }
        MainActivity.buffer.clear();
        MainActivity.regist();
        hideMenu();
        AlbumDetailAdapter.delMode = false;

    }
    @Override
    public void onBackPressed() {
        if(AlbumDetailAdapter.delMode == true){
            MainActivity.buffer.clear();
            hideMenu();
            AlbumDetailAdapter.delMode = false;
            AlbumDetailAdapter.unCheckAll();
            albumDetailAdapter.notifyDataSetChanged();
            return;
        }
        MainActivity.mainMode = true;
        MainActivity.clearDelMode();
        super.onBackPressed();

    }
    public void showDeleteAlbumDialog(){
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.delete_item ) + "?")
                    .setNegativeButton(getString(R.string.no), null)
                    .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteAlbum();
                        }
                    })
                    .create().show();
    }
    public void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_item))
                .setNegativeButton(getString(R.string.no), null)
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteItems();
                    }
                })
                .create().show();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            MainActivity.padding = 3;
            // setContentView(R.layout.yourxmlinlayout-land);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            MainActivity.padding = 0;
        }
        refesh();

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseActivity.changeLanguage(MainActivity.language, this);
        BaseActivity.changeTheme(MainActivity.light, this);
        if (MainActivity.curAlbum.getType() == Album.typePrivate && unlocked == false ){
            if (MainActivity.password.equals("")){
                Intent i = new Intent(this, CreatePassActivity.class);
                startActivityForResult(i, CREATE_PASS);
            }
            else {
                Intent i = new Intent(this, PrivateAlbumActivity.class);
                startActivityForResult(i, PRIVATE_ALBUM);
            }
        }
        else
            init();

    }
    void init(){
        MainActivity.mainMode = false;
        MainActivity.clearDelMode();
        setContentView(R.layout.activity_view_album);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar = getSupportActionBar();
        TextView name = findViewById(R.id.albumName);
        name.setText(MainActivity.curAlbum.getName());
        context = this;
        this.name = MainActivity.curAlbum.getName();
        adapters = MainActivity.getAllDateAdapter(MainActivity.curAlbum.getImages(), this);
        albumDetailAdapter = new AlbumDetailAdapter(adapters, context, MainActivity.numCol + MainActivity.padding);
        listView = (ListView) findViewById(R.id.album);
        listView.setAdapter(albumDetailAdapter);
    }
    static public void refesh(){
        if(name.equals(""))
            return;
        if (MainActivity.curAlbum == null)
            return;
        if(name.equals("Private")){
            MainActivity.curAlbum = MainActivity.privateAlbum;
        }else{
            for(Album album: MainActivity.albums)
                if (album.getName().equals(name)){
                    MainActivity.curAlbum = album;
                    break;
                }
        }

        adapters = MainActivity.getAllDateAdapter(MainActivity.curAlbum.getImages(), context);
        albumDetailAdapter = new AlbumDetailAdapter(adapters, context, MainActivity.numCol + MainActivity.padding);
        listView.setAdapter(albumDetailAdapter);
        albumDetailAdapter.notifyDataSetChanged();

    }
    void showCopyOrMove(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.choose_copy_move))

                .setNeutralButton( R.string.cancel, null)
                .setNegativeButton(R.string.copy , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent1 = new Intent(ViewAlbumActivity.this, ChooseAlbum.class);
                        startActivityForResult(intent1, CHOOSE_ALBUM);
                    }
                })
                .setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent1 = new Intent(ViewAlbumActivity.this, ChooseAlbum.class);
                        startActivityForResult(intent1, CHOOSE_ALBUM_TO_MOVE);
                    }
                })
                .create().show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED){
            if (requestCode == CHOOSE_ALBUM)
                return;
            if(requestCode != CHECK_PASS && requestCode != CHANGE_PASS ){

                finish();
            }
        }
        if (requestCode == PRIVATE_ALBUM || requestCode == CREATE_PASS){
            unlocked = true;
            init();
        }
        if (requestCode == CHECK_PASS){
            if (resultCode == RESULT_OK){
                Intent i = new Intent(this, CreatePassActivity.class);
                startActivityForResult(i, CHANGE_PASS);
            }
        }
        if (requestCode == CHOOSE_ALBUM || requestCode == CHOOSE_ALBUM_TO_MOVE)
        {
            if (data == null)
                return;
            int i = data.getIntExtra("id_album_choose", -2);
            if (i < -1)
                return;
            MainActivity.unregist();

            IntentFilter mainFilter;
            if (CHOOSE_ALBUM == requestCode){
                service = new Intent(this, CopyItemsService.class);
                mainFilter = new IntentFilter("matos.action.GOSERVICE4");
                receiver = new MyMainLocalReceiver();
            }
            else{
                service = new Intent(this, MoveItemsService.class);
                mainFilter = new IntentFilter("matos.action.GOSERVICE5");
                receiver = new MyMainLocalReceiver2();
            }

            registerReceiver(receiver, mainFilter);
            String nameNewAlbum = "";
            if (i != -1)
                nameNewAlbum = MainActivity.albums.get(i).getPath();
            else
                nameNewAlbum = MainActivity.privateAlbum.getPath();
            service.putExtra("path", nameNewAlbum);
            startService(service);

            n = MainActivity.buffer.size();
            n = MainActivity.buffer.size();
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.hide), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    hideMenu();
                    mProgressDialog.dismiss();
                }
            });
            mProgressDialog.show();
        }
    }

     class MyMainLocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context localContext, Intent callerIntent) {

            int progress = callerIntent.getIntExtra("number", 0); //get the progress
            System.out.println(progress);
            mProgressDialog.setProgress(progress * 100 / n);
            if (progress == n) {
                hideMenu();
                MainActivity.loadAllFiles();
                unregisterReceiver(receiver);
                MainActivity.buffer.clear();
                MainActivity.regist();
                hideMenu();
                AlbumDetailAdapter.delMode =false;
                refesh();
                MainActivity.refesh();
                MainActivity.regist();
                hideMenu();
                MainActivity.buffer.clear();

                mProgressDialog.dismiss();
            }
        }
    }
    class MyMainLocalReceiver2 extends BroadcastReceiver {
        @Override
        public void onReceive(Context localContext, Intent callerIntent) {

            int progress = callerIntent.getIntExtra("number", 0); //get the progress
            System.out.println(progress);
            mProgressDialog.setProgress(progress * 100 / n);
            if (progress == n) {
                for(Item item: MainActivity.buffer)
                    item.delete(ViewAlbumActivity.this, MainActivity.curAlbum.getType() == Album.typePrivate);
                MainActivity.loadAllFiles();
                unregisterReceiver(receiver);
                MainActivity.buffer.clear();
                MainActivity.regist();
                hideMenu();
                AlbumDetailAdapter.delMode = false;
                refesh();
                MainActivity.refesh();
                MainActivity.regist();
                MainActivity.buffer.clear();

                mProgressDialog.dismiss();
            }
        }
    }
}