package com.example.gallery.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.loader.content.CursorLoader;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gallery.CameraEventReceiver;
import com.example.gallery.CopyItemsService;
import com.example.gallery.MoveItemsService;
import com.example.gallery.R;
import com.example.gallery.adapter.AlbumDetailAdapter;
import com.example.gallery.adapter.DateAdapter;
import com.example.gallery.fragment.Fragment1;
import com.example.gallery.fragment.Fragment2;
import com.example.gallery.fragment.Fragment3;
import com.example.gallery.model.Album;
import com.example.gallery.model.Image;
import com.example.gallery.model.Item;
import androidx.exifinterface.media.ExifInterface;
import com.example.gallery.model.Video;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class MainActivity extends BaseActivity {
    private static final int SETTING_CODE = 1442;
    Intent service;
    int c = 0;

    BroadcastReceiver receiver;
    int n = 1;
    ProgressDialog mProgressDialog;
    static public  String language = "vi";
    static public boolean light = true;
    private static final int REQUEST_VIDEO_CAPTURE = 111;
    private static final int CHOOSE_ALBUM = 41;
    private static final int CHOOSE_ALBUM_TO_MOVE = 67;
    TabLayout tabLayout;
    ViewPager viewPager;
    static public Fragment1 fragment1;
    public static Fragment2 fragment2;
    Fragment3 fragment3;

    String[] listPermissions=new String[]{Manifest.permission.CAMERA};
    static public ArrayList<Album> albums = new ArrayList<>();
    static public ArrayList<Item> items = new ArrayList<>();
    static public ArrayList<Item> itemsTmp = new ArrayList<>();
    public static final int PERMISSION_REQUEST_CODE = 100;
    public static final int CAMERA_PERMISSION_CODE = 300;
    public static final int UPDATED_CODE = 222;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static public MenuItem checkAll;
    static public MenuItem del;
    static public MenuItem camera;
    static public MenuItem record;
    static public Menu menu;
    public static String password = "";
    static public ActionBar actionBar;
    static  public boolean hideDate = false;
    static SharedPreferences.Editor prefsEditor;
    static PagerAdapter adapter;
    static public ArrayList<String> listLove = new ArrayList<>();
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    static public int numCol = 4;
    static public SharedPreferences prefs;
    static public boolean mainMode = true;
    static public Context context;
    static public boolean deleteChange = false;
    static public Album curAlbum = null;
    static public MenuItem addTo;
    static public ContentObserver contentObserver;
    final static public String rootDir = "/storage/emulated/0/";
    static public Album loveAlbum = new Album("Love");
    static public Album privateAlbum = new Album("Private");
    static public ArrayList<Item> buffer = new ArrayList<>();
    private String privateFolder;

    static public void showMenu(){
        addTo.setVisible(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        checkAll.setVisible(true);
        del.setVisible(true);
        record.setVisible(false);
        camera.setVisible(false);
        menu.setGroupVisible(R.id.group, false);
    }
    static public void hideMenu(){
        addTo.setVisible(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        checkAll.setVisible(false);
        del.setVisible(false);
        record.setVisible(true);
        camera.setVisible(true);
        menu.setGroupVisible(R.id.group, true);
    }
    boolean checkPermission(String per){
        if(ContextCompat.checkSelfPermission(this,per)!=PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu, menu);
        checkAll = menu.findItem(R.id.checkAll);
        del = menu.findItem(R.id.del);
        camera = menu.findItem(R.id.camera);
        record = menu.findItem(R.id.record);

        addTo = menu.findItem(R.id.addTo);
        hideMenu();
        registerReceiver(new CameraEventReceiver(), new IntentFilter(Camera.ACTION_NEW_PICTURE));
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
                fragment1.getAlbumDetailAdapter().notifyDataSetChanged();
            }
        });

        return true;
    }
    private void deleteItems(){
        unregist();
           for(Item item : buffer){
               item.delete(context, false);
          }
        buffer.clear();
        hideMenu();
        loadAllFiles();
        refesh();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        AlbumDetailAdapter.delMode = (AlbumDetailAdapter.delMode + 1) % 2;
        regist();
    }
    public void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_item ) + "?")
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.camera:
                if(!checkPermission(Manifest.permission.CAMERA))
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(listPermissions,CAMERA_PERMISSION_CODE);
                    }
                }else {
                    dispatchTakePictureIntent(false);
                }
                break;
            case R.id.addTo:
                if(MainActivity.buffer.size() == 0)
                    break;
                showCopyOrMove();
                break;
            case R.id.record:
                if(!checkPermission(Manifest.permission.CAMERA))
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(listPermissions,CAMERA_PERMISSION_CODE);
                    }
                }else {
                    dispatchTakePictureIntent(true);
                }
                break;
            case R.id.love:
                MainActivity.curAlbum = loveAlbum;
                Intent intent = new Intent((Activity) context, ViewAlbumActivity.class);
                ((Activity) context).startActivity(intent);
                break;
            case R.id.pri:
                MainActivity.curAlbum = privateAlbum;
                Intent intent1 = new Intent((Activity) context, ViewAlbumActivity.class);
                ((Activity) context).startActivity(intent1);
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.del:
                showDeleteDialog();
                break;
            case R.id.show_hideDate:
                if (hideDate)
                    hideDate = false;
                else
                    hideDate = true;
                prefsEditor.putBoolean("hideDate", hideDate);
                prefsEditor.apply();
                refesh();
                break;
            case R.id.setting:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, SETTING_CODE);

                break;
            case R.id.small:
                numCol = AlbumDetailAdapter.small;
                prefsEditor.putInt("numCol", numCol);
                prefsEditor.apply();
                refesh();
                break;
            case R.id.medium:
                numCol = AlbumDetailAdapter.medium;
                prefsEditor.putInt("numCol", numCol);
                prefsEditor.apply();
                refesh();

                break;
            case R.id.large:
                numCol = AlbumDetailAdapter.large;
                prefsEditor.putInt("numCol", numCol);
                prefsEditor.apply();
                refesh();
                break;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }
    void showCopyOrMove(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_item )

                .setNeutralButton(R.string.cancel , null)
                .setNegativeButton(R.string.copy , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent1 = new Intent(MainActivity.this, ChooseAlbum.class);
                        startActivityForResult(intent1, CHOOSE_ALBUM);
                    }
                })
                .setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent1 = new Intent(MainActivity.this, ChooseAlbum.class);
                        startActivityForResult(intent1, CHOOSE_ALBUM_TO_MOVE);
                    }
                })
                .create().show();
    }
    static  public void refesh(){
        if(fragment1 == null || fragment2 == null)
            return;
        fragment1.setNumCol(numCol);
        fragment1.setAdapters(getAllDateAdapter(items, context));
        fragment1.getAlbumDetailAdapter().notifyDataSetChanged();
        fragment2.setAlbums(albums);
        fragment2.getAdapter().notifyDataSetChanged();
        regist();
    }

    public static ArrayList<DateAdapter> getAllDateAdapter(ArrayList<Item> items, Context context) {

        ArrayList<DateAdapter> adapters = new ArrayList<>();
        if(items.size() ==0)
            return adapters;
        if (!hideDate){
            String currentDate = null;
            ArrayList<Item> temp = new ArrayList<>();
            for (Item item : items) {
                if(item == null)
                    continue;
                if (currentDate == null){
                    currentDate = item.getAddedDate();
                    temp.add(item);
                }
                else {
                    if (!item.getAddedDate().equals(currentDate)) {
                        adapters.add(new DateAdapter(currentDate, temp, context));
                        currentDate = item.getAddedDate();
                        temp = new ArrayList();
                        temp.add(item);
                    } else {
                        temp.add(item);
                    }
                }
            }
            if (!temp.isEmpty())
                adapters.add(new DateAdapter(currentDate, temp, context));

        }
        else{
            adapters.add(new DateAdapter("", items, context));
        }
        return adapters;
    }

    @Override
    public void onBackPressed() {
        if(AlbumDetailAdapter.delMode == 1){
            buffer.clear();
            hideMenu();
            AlbumDetailAdapter.delMode = (AlbumDetailAdapter.delMode + 1) % 2;
            AlbumDetailAdapter.unCheckAll();
            fragment1.getAlbumDetailAdapter().notifyDataSetChanged();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            return;
        }
        super.onBackPressed();

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   // Toast.makeText(this, "Yeah external permission granted", Toast.LENGTH_SHORT).show();

                    init();

                } else {
                    Snackbar snackbar = getPermissionDeniedSnackbar(findViewById(R.id.root_view));
                    snackbar.setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkPermission(MainActivity.this);
                        }
                    });
                    showSnackbar(snackbar);
                }
                break;

            case CAMERA_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                    dispatchTakePictureIntent(false);
                } else {
                   // Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();

                }
                break;
        }
    }
    static public  void saveArrayList(ArrayList<String> list, String key){
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();

    }

    static public ArrayList<String> getArrayList(String key){

        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        if (json == null)
            return new ArrayList<>();
        String [] res = gson.fromJson(json, String[].class);
        return new ArrayList<>(Arrays.asList(res));
    }
    private Long readLastDateFromMediaStore(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, "date_added DESC");

        if (cursor.moveToNext()) {
            Long dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED));
        }
        cursor.close();
        return null;
    }
    void changeLanguage (String language){

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        this.getResources().updateConfiguration(configuration,
                this.getResources().getDisplayMetrics());
    }
    void changeTheme(boolean light){
        if (light) {
            this.setTheme(R.style.LightTheme);
        } else {
            this.setTheme(R.style.DarkTheme);
        }
    }
    static public boolean reloadPager = false;
    public void init()  {
        context = this;

        contentObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange, Uri uri) {

            }
        };
        regist();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                String l = prefs.getString("language", "vi");
                boolean change = false;
                if (!l.equals(language)){
                    language = l;
                    changeLanguage(language);
                    change =true;
                }
                boolean t = prefs.getBoolean("theme", true);
                if (t != light){
                    light = t;
                    changeTheme(light);
                    change =true;
                }
                if (change){
                    finish();
                    startActivity(getIntent());
                }

            }
        });
        prefsEditor = prefs.edit();
        listLove = getArrayList("listLove");
        loveAlbum.setType(Album.typeLove);
        light = prefs.getBoolean("theme", true);
        language = prefs.getString("language" , "vi");
        changeLanguage(language);
        changeTheme(light);
        setContentView(R.layout.activity_gallery);
        numCol = prefs.getInt("numCol", 4);
        hideDate = prefs.getBoolean("hideDate", false);
        password = prefs.getString("pass", "");
        loadAllFiles();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        adapter = new PagerAdapter(getSupportFragmentManager());
        actionBar = getSupportActionBar();
        fragment1 = new Fragment1(getAllDateAdapter(items, context), numCol);
        fragment2 = new Fragment2(albums);
        fragment3 = new Fragment3(items);
        adapter.addFragment(fragment1, getString(R.string.photo_video));
        adapter.addFragment(fragment2, "Album");
        adapter.addFragment(fragment3, getString(R.string.people));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!checkPermission(this))
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},1000);
        else
            init();
    }
    public static Snackbar getPermissionDeniedSnackbar(final View rootView) {
        Snackbar snackbar = Snackbar.make(rootView,
                R.string.read_permission_denied,
                Snackbar.LENGTH_INDEFINITE);
        snackbar.getView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(rootView.getContext(), R.string.read_permission_denied, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        return snackbar;
    }

    @NonNull
    public Uri saveBitmap(@NonNull final Context context, @NonNull final Bitmap bitmap,
                          @NonNull final Bitmap.CompressFormat format,
                          @NonNull final String mimeType,
                          @NonNull final String displayName) throws IOException {

        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);

        final ContentResolver resolver = context.getContentResolver();
        Uri uri = null;

        try {
            final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uri = resolver.insert(contentUri, values);

            if (uri == null)
                throw new IOException("Failed to create new MediaStore record.");

            try (final OutputStream stream = resolver.openOutputStream(uri)) {
                if (stream == null)
                    throw new IOException("Failed to open output stream.");

                if (!bitmap.compress(format, 100, stream))
                    throw new IOException("Failed to save bitmap.");
            }

            return uri;
        }
        catch (IOException e) {

            if (uri != null) {
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(uri, null, null);
            }

            throw e;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SETTING_CODE){
                changeLanguage(language);
                changeTheme(light);
                finish();
                startActivity(getIntent());
            }
            if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_VIDEO_CAPTURE) {
                unregist();
                if (requestCode == REQUEST_IMAGE_CAPTURE){
                    Bundle extras = data.getExtras();
                    
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    String name = "IMAGE_"+ new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    try {
                        saveBitmap(this, imageBitmap, Bitmap.CompressFormat.JPEG, "image/jpeg",name);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                notifyAddNewFile(newItem());
                refesh();

            }
            if (requestCode == CHOOSE_ALBUM || requestCode == CHOOSE_ALBUM_TO_MOVE)
            {
                int i = data.getIntExtra("id_album_choose", -1);
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
                if (i != - 1)
                    nameNewAlbum  = MainActivity.albums.get(i).getPath();
                else
                    nameNewAlbum  = MainActivity.privateAlbum.getPath();
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
                        mProgressDialog.dismiss();
                        hideMenu();
                    }
                });
                mProgressDialog.show();
            }

        }
    }
    private Item newItem() {
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DURATION,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
        };

        // Return only video and image metadata.
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        Uri queryUri = MediaStore.Files.getContentUri("external");

        CursorLoader cursorLoader = new CursorLoader(
                this,
                queryUri,
                projection,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC LIMIT 1" // Sort order.
        );

        Cursor cursor = cursorLoader.loadInBackground();
        Item item = null;
        if (cursor.moveToNext()) {
            String absolutePathOfFile = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            Long longDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED));
            Long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
            Long durationNum = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION));
            String addedDate = DateFormat.format("dd/MM/yyyy", new Date(longDate * 1000)).toString();
            String albumName = cursor.getString((cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)));

            ExifInterface exif;
            try {

                exif = new ExifInterface(absolutePathOfFile);
                float[] latLng = new float[2];
                exif.getLatLong(latLng);

                if (Image.isImageFile(absolutePathOfFile))
                    item = new Image(id, absolutePathOfFile, addedDate, false);
                if (Image.isVideoFile(absolutePathOfFile)) {
                    item = new Video(id, absolutePathOfFile, addedDate, Image.convertToDuration(durationNum), durationNum, false);
                }
                Album album = MainActivity.existsAlbum(albumName);
                if (album == null) {
                    album = new Album(albumName);
                    MainActivity.albums.add(album);
                }
                album.addHead(item);
            } catch (Exception ex) {
                System.out.println(absolutePathOfFile);
            }
        }
        cursor.close();
        return item;
    }
    static public void notifyAddNewFile(Item item){
        MainActivity.items.add(0, item);
        ArrayList<DateAdapter> adapters = fragment1.getAlbumDetailAdapter().getList();
        if(adapters.size()==0)
           return;
        Item first = adapters.get(0).getImages().get(0);
        if (item.getAddedDate().equals(first.getAddedDate())){
            adapters.get(0).getImages().add(0, item);
            adapters.get(0).notifyDataSetChanged();
        }
        else{
            ArrayList<Item> l = new ArrayList<>();
            l.add(item);
            fragment1.getAlbumDetailAdapter().getList().add(0, new DateAdapter(item.getAddedDate(), l, context));
            fragment1.getAlbumDetailAdapter().notifyDataSetChanged();
        }
        fragment2.setAlbums(albums);
        fragment2.getAdapter().notifyDataSetChanged();
    }

    private void dispatchTakePictureIntent(boolean video) {
        if (video){
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
        else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    private class PagerAdapter extends FragmentPagerAdapter {
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<Fragment> fragments = new ArrayList<>();

        void addFragment(Fragment fragment, String title) {
            titles.add(title);
            fragments.add(fragment);
        }

        PagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    public static final String SNACKBAR = "SNACKBAR";
    public static void showSnackbar(Snackbar snackbar) {
        snackbar.getView().setTag(SNACKBAR);
        TextView textView = snackbar.getView()
                .findViewById(R.id.snackbar_text);
        textView.setTypeface(ResourcesCompat
                .getFont(textView.getContext(), R.font.roboto_mono_medium));
        snackbar.show();
    }
    public static boolean checkPermission(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int read = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
            int write = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (read != PackageManager.PERMISSION_GRANTED || write != PackageManager.PERMISSION_GRANTED) {
                String[] requestedPermissions = new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(context, requestedPermissions, PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }
    static private Cursor read(){
        items.clear();
        albums.clear();
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DURATION,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
        };

        // Return only video and image metadata.
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        Uri queryUri = MediaStore.Files.getContentUri("external");

        CursorLoader cursorLoader = new CursorLoader(
                context,
                queryUri,
                projection,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC " // Sort order.
        );

        Cursor cursor = cursorLoader.loadInBackground();
        return cursor;
    }
    static void load(Cursor cursor){
        while (cursor.moveToNext()) {
            String absolutePathOfFile = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            Long longDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED));
            Long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
            Long durationNum = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION));
            String addedDate = DateFormat.format("dd/MM/yyyy", new Date(longDate * 1000)).toString();
            String albumName = cursor.getString((cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)));
            ExifInterface exif;
            try{

                exif = new ExifInterface(absolutePathOfFile);
                float[] latLng = new float[2];
                exif.getLatLong(latLng);

                Geocoder geocoder = new Geocoder(context);
                List<Address> addresses = geocoder.getFromLocation(latLng[0], latLng[1], 1);

                boolean isLoved = isInLoveList(absolutePathOfFile, MainActivity.listLove);

                Item item = null;
                if (Image.isImageFile(absolutePathOfFile)){
                    item = new Image(id, absolutePathOfFile,  addedDate, isLoved);

                }
                else if (Image.isVideoFile(absolutePathOfFile)) {
                    item = new Video(id, absolutePathOfFile,  addedDate, Image.convertToDuration(durationNum), durationNum, isLoved);
                }
                if(isLoved)
                    loveAlbum.addItem(item);
                items.add(item);
                Album album = existsAlbum(albumName);
                if (album == null){
                    album = new Album(albumName);
                    albums.add(album);
                }
                album.addItem(item);

            }
            catch (Exception ex){
                System.out.println(absolutePathOfFile);
            }
        }
        cursor.close();
    }
    static public void loadAllFiles() {
        Cursor cursor = read();
        load(cursor);
        loadPrivate();

    }
    static public boolean isInLoveList(String absolutePathOfFile, ArrayList<String> listLove){
        for(String path:listLove){
            if (path.equals(absolutePathOfFile))
                return true;
        }
        return false;
    }

    // return specific album with given name
    static public Album existsAlbum(String albumName) {
        for(Album album:albums){
            if (album.getName().equals(albumName))
                return album;
        }
        return null;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
           // setContentView(R.layout.yourxmlinlayout-land);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //setContentView(R.layout.yourxmlinlayoutfolder);
        }
    }
    static void clearDelMode(){
        hideMenu();
        buffer.clear();
        AlbumDetailAdapter.delMode = 0;
    }
    static public void regist(){
        context.getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
                contentObserver);
    }
    static public void unregist(){
        context.getContentResolver().unregisterContentObserver(contentObserver);
    }
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }
    public ArrayList<String> listFilesForFolder(final File folder) {
        ArrayList<String> paths = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                paths.add(fileEntry.getAbsolutePath());
            }
        }
        return  paths;
    }
    static public boolean checkPass(String pass){
        return pass.equals(password);
    }
    static public void loadPrivate() {
        privateAlbum.getImages().clear();
        privateAlbum.setType(Album.typePrivate);
        File mydir = context.getDir("myprivatefolder", Context.MODE_PRIVATE); //Creating an internal dir;
        if (!mydir.exists()) {
            mydir.mkdirs();
        }
        privateAlbum.setPath(mydir.getAbsolutePath() + "/");
        File[] files = mydir.listFiles();
        for (File file : files) {
            FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();

            Uri uri = Uri.fromFile(file);
            //retriever.setDataSource(file.getAbsolutePath());


          //  String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationNum = 0;//Long.parseLong(retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION));
            Item item = null;
            String path = file.getAbsolutePath();
            String addedDate = "";//retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DATE);
            if (Image.isImageFile(path)){
                item = new Image(new Long(0), path,  addedDate, false);

            }
            else if (Image.isVideoFile(path)) {
                item = new Video(new Long(0), path,  addedDate, Image.convertToDuration(durationNum), durationNum, false);
            }
            privateAlbum.addItem(item);
            retriever.release();
        }

    }
    class MyMainLocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context localContext, Intent callerIntent) {

            int progress = callerIntent.getIntExtra("number", 0); //get the progress
            System.out.println(progress);
            mProgressDialog.setProgress(progress * 100 / n);
            if (progress == n) {

                AlbumDetailAdapter.delMode = (AlbumDetailAdapter.delMode + 1) % 2;
                hideMenu();
                MainActivity.loadAllFiles();
                unregisterReceiver(receiver);
                MainActivity.refesh();
                MainActivity.regist();
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
                    item.delete(MainActivity.this, MainActivity.curAlbum.getType() == Album.typePrivate);
                MainActivity.loadAllFiles();
                unregisterReceiver(receiver);
                MainActivity.refesh();
                MainActivity.regist();
                mProgressDialog.dismiss();
                hideMenu();
                MainActivity.buffer.clear();
            }
        }
    }
}