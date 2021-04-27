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
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
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
import com.example.gallery.model.Image;
import com.example.gallery.model.Item;
import com.example.gallery.model.Video;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_VIDEO_CAPTURE = 111;
    TabLayout tabLayout;
    ViewPager viewPager;
    String[] listPermissions=new String[]{Manifest.permission.CAMERA};
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
    static public ActionBar actionBar;
    PagerAdapter adapter;
    static public void showMenu(){
        actionBar.setDisplayHomeAsUpEnabled(true);
        checkAll.setVisible(true);
        del.setVisible(true);
        record.setVisible(false);
        camera.setVisible(false);
        menu.setGroupVisible(R.id.group, false);
    }
    static public void hideMenu(){
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
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

    void refesh(){
        Fragment1 fragment1 = (Fragment1) adapter.getItem(0);
        fragment1.setAdapters(getAllDateAdapter(items));
        fragment1.getAlbumDetailAdapter().notifyDataSetChanged();
    }
    public ArrayList<DateAdapter> getAllDateAdapter(ArrayList<Item> items) {
        ArrayList<DateAdapter> adapters = new ArrayList<>();
        if(items.size() ==0)
            return adapters;
        String currentDate = null;
        ArrayList<Item> temp = new ArrayList<>();
        for (Item item : items) {
            if (currentDate == null){
                currentDate = item.getAddedDate();
                temp.add(item);
            }
            else {
                if (!item.getAddedDate().equals(currentDate)) {
                    adapters.add(new DateAdapter(currentDate, temp, this));
                    currentDate = item.getAddedDate();
                    temp = new ArrayList();
                    temp.add(item);
                } else {
                    temp.add(item);
                }
            }
        }
        if (!temp.isEmpty())
            adapters.add(new DateAdapter(currentDate, temp, this));
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
            AlbumDetailAdapter.notifyChanged();
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

    public void init()  {
        loadAllFiles();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        adapter = new PagerAdapter(getSupportFragmentManager());
        actionBar = getSupportActionBar();
        adapter.addFragment(new Fragment1(getAllDateAdapter(items)), "Ảnh/Video");
        adapter.addFragment(new Fragment2(items), "Album");
        adapter.addFragment(new Fragment3(items), "Người");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        //Request access storage permission
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



    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
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
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC " // Sort order.
        );

        Cursor cursor = cursorLoader.loadInBackground();
        while (cursor.moveToNext()) {
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

                for (Address add : addresses) {
                    String address = addresses.get(0).getAddressLine(0);
                }

                if (Image.isImageFile(absolutePathOfFile))
                    items.add(new Image(id, absolutePathOfFile,  addedDate));
                if (Image.isVideoFile(absolutePathOfFile)) {
                    items.add(new Video(id, absolutePathOfFile,  addedDate, Image.convertToDuration(durationNum)));
                }
            }
            catch (Exception ex){
                // ex.printStackTrace();
                System.out.println(absolutePathOfFile);
            }
        }
        cursor.close();
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


                if (Image.isImageFile(absolutePathOfFile))
                    item = new Image(id, absolutePathOfFile,  addedDate);
                if (Image.isVideoFile(absolutePathOfFile)) {
                    item = new Video(id, absolutePathOfFile,  addedDate, Image.convertToDuration(durationNum));
                }
            }
            catch (Exception ex){
                System.out.println(absolutePathOfFile);
            }
        }
        cursor.close();
        return item;
    }
}