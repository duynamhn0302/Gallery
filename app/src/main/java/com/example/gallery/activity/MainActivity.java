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
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.loader.content.CursorLoader;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gallery.R;
import com.example.gallery.adapter.AlbumDetailAdapter;
import com.example.gallery.adapter.DateAdapter;
import com.example.gallery.fragment.Fragment1;
import com.example.gallery.fragment.Fragment2;
import com.example.gallery.fragment.Fragment3;
import com.example.gallery.model.Album;
import com.example.gallery.model.Image;
import com.example.gallery.model.Item;
import com.example.gallery.model.Video;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_VIDEO_CAPTURE = 111;
    TabLayout tabLayout;
    ViewPager viewPager;
    static public Fragment1 fragment1;
    Fragment2 fragment2;
    Fragment3 fragment3;
    String[] listPermissions=new String[]{Manifest.permission.CAMERA};
    static public ArrayList<Album> albums = new ArrayList<>();
    static public ArrayList<Item> originalItems = new ArrayList<>();
    static public ArrayList<Item> items = new ArrayList<>();
    public static final int PERMISSION_REQUEST_CODE = 100;
    public static final int CAMERA_PERMISSION_CODE = 300;
    public static final int UPDATED_CODE = 222;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static public MenuItem checkAll;
    static public MenuItem del;
    static public MenuItem camera;
    static public MenuItem record;
    static public Menu menu;
    static public boolean checkAllFlag = false;
    static public boolean unCheckAllFlag = true;
    static public ActionBar actionBar;
    static  public boolean hideDate = false;
    static SharedPreferences.Editor prefsEditor;
    static PagerAdapter adapter;
    static public ArrayList<String> listLove = new ArrayList<>();
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    static int numCol = 4;
    static public SharedPreferences prefs;
    static public boolean mainMode = true;
    static public Context context;

    static public void showMenu(){
        actionBar.setDisplayHomeAsUpEnabled(true);
        checkAll.setVisible(true);
        del.setVisible(true);
        record.setVisible(false);
        camera.setVisible(false);
        menu.setGroupVisible(R.id.group, false);
    }
    static public void hideMenu(){
        unCheckAllFlag = true;
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
                fragment1.getAlbumDetailAdapter().notifyDataSetChanged();
            }
        });

        return true;
    }
    private void deleteItems(){
        AlbumDetailAdapter.countCheck = 0;
        for(int i = items.size() - 1; i >= 0; i--){
            if (items.get(i).getChecked()){
                File file = new File(items.get(i).getFilePath());
                if(!file.delete()){
                    System.out.println("K xoa dc");
                }
                items.remove(i);
            }
        }
        refesh();
        hideMenu();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        AlbumDetailAdapter.delMode = (AlbumDetailAdapter.delMode + 1) % 2;
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

    static public void refesh(){
        Fragment1 fragment1 = (Fragment1) adapter.getItem(0);
        fragment1.setNumCol(numCol);
        fragment1.setAdapters(getAllDateAdapter(items, context));
        fragment1.getAlbumDetailAdapter().notifyDataSetChanged();
    }
    public static ArrayList<DateAdapter> getAllDateAdapter(ArrayList<Item> items, Context context) {

        ArrayList<DateAdapter> adapters = new ArrayList<>();
        if(items.size() ==0)
            return adapters;
        if (!hideDate){
            String currentDate = null;
            ArrayList<Item> temp = new ArrayList<>();
            for (Item item : items) {
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
            adapters.add(new DateAdapter("", MainActivity.items, context));
        }
        return adapters;
    }

    @Override
    public void onBackPressed() {
        if(AlbumDetailAdapter.delMode == 1){
            for(Item item:items)
                item.setChecked(false);
            AlbumDetailAdapter.countCheck = 0;
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
                    Toast.makeText(this, "Yeah external permission granted", Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                    dispatchTakePictureIntent(false);
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();

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
    public void init()  {
        context = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefsEditor = prefs.edit();
        listLove = getArrayList("listLove");
        numCol = prefs.getInt("numCol", 4);
        hideDate = prefs.getBoolean("hideDate", false);
        loadAllFiles();
        originalItems.addAll(items);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        adapter = new PagerAdapter(getSupportFragmentManager());
        actionBar = getSupportActionBar();
        fragment1 = new Fragment1(getAllDateAdapter(items, context), numCol);
        fragment2 = new Fragment2(albums);
        fragment3 = new Fragment3(items);
        adapter.addFragment(fragment1, "Ảnh/Video");
        adapter.addFragment(fragment2, "Album");
        adapter.addFragment(fragment3, "Người");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_VIDEO_CAPTURE) {
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
                items.add(0, newItem());
                refesh();


            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
            }
            if (requestCode == UPDATED_CODE) {

                refesh();

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
            }

        }


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
    // load all files from internal storage to array items
    public void loadAllFiles() {
        items.clear();
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
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC " // Sort order.
        );

        Cursor cursor = cursorLoader.loadInBackground();
        while (cursor.moveToNext()) {
            String absolutePathOfFile = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
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

                Geocoder geocoder = new Geocoder(this);
                List<Address> addresses = geocoder.getFromLocation(latLng[0], latLng[1], 1);

                for (Address add : addresses) {
                    String address = addresses.get(0).getAddressLine(0);
                }
                boolean isLoved = isInLoveList(absolutePathOfFile, MainActivity.listLove);
                Item item = null;
                if (Image.isImageFile(absolutePathOfFile)){
                    item = new Image(id, absolutePathOfFile,  addedDate, isLoved);
                }
                if (Image.isVideoFile(absolutePathOfFile)) {
                    item = new Video(id, absolutePathOfFile,  addedDate, Image.convertToDuration(durationNum), isLoved);
                }
                items.add(item);
                Album album = existsAlbum(albumName);
                if (album == null){
                    album = new Album(albumName);
                    albums.add(album);
                }
                album.addItem(item);

            }
            catch (Exception ex){
                // ex.printStackTrace();
                System.out.println(absolutePathOfFile);
            }
        }
        cursor.close();
    }
    boolean isInLoveList(String absolutePathOfFile, ArrayList<String> listLove){
        for(String path:listLove){
            if (path.equals(absolutePathOfFile))
                return true;
        }
        return false;
    }
    private Item newItem()  {
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DURATION,
                MediaStore.Files.FileColumns.TITLE
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

            ExifInterface exif;
            try{

                exif = new ExifInterface(absolutePathOfFile);
                float[] latLng = new float[2];
                exif.getLatLong(latLng);

                Geocoder geocoder = new Geocoder(this);
                List<Address> addresses = geocoder.getFromLocation(latLng[0], latLng[1], 1);

                boolean isLoved = isInLoveList(absolutePathOfFile, MainActivity.listLove);

                if (Image.isImageFile(absolutePathOfFile))
                    item = new Image(id, absolutePathOfFile,  addedDate, false);
                if (Image.isVideoFile(absolutePathOfFile)) {
                    item = new Video(id, absolutePathOfFile,  addedDate, Image.convertToDuration(durationNum), false);
                }
            }
            catch (Exception ex){
                System.out.println(absolutePathOfFile);
            }
        }
        cursor.close();
        return item;
    }
    // chỗ này
    // load all albums from internal storage to array albums

    // return specific album with given name
    public Album existsAlbum(String albumName) {
        for(Album album:albums){
            if (album.getName().equals(albumName))
                return album;
        }
        return null;
    }
    static void clearDelMode(){
        hideMenu();
        AlbumDetailAdapter.countCheck = 0;
        AlbumDetailAdapter.delMode = 0;
    }
}