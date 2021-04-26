package com.example.gallery.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.loader.content.CursorLoader;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.gallery.R;
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
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    String[] listPermissions=new String[]{Manifest.permission.CAMERA};
    static ArrayList<Item> items = new ArrayList<>();
    public static final int PERMISSION_REQUEST_CODE = 100;
    public static final int CAMERA_PERMISSION_CODE = 300;
    public static final int UPDATED_CODE = 222;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    PagerAdapter adapter;
    boolean checkPermission(String per){
        if(ContextCompat.checkSelfPermission(this,per)!=PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    String currentPhotoPath;




    private void dispatchTakePictureIntent() {
        Intent i = new Intent();
        i.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivity(i);
        } else {
            Toast.makeText(MainActivity.this, "ád", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(currentPhotoPath);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {


                // here you will get the image as bitmap


            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
            }
            if (requestCode == UPDATED_CODE) {


                loadAllFiles();
                Fragment1 fragment1 = (Fragment1) adapter.getItem(0);
                fragment1.setAdapters(getAllDateAdapter(items));
                fragment1.getAlbumDetailAdapter().notifyDataSetChanged();

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
            }

        }


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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.camera:
                if(!checkPermission(Manifest.permission.CAMERA)||!checkPermission(Manifest.permission.READ_CONTACTS))
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(listPermissions,CAMERA_PERMISSION_CODE);
                    }
                }else {
                    dispatchTakePictureIntent();
                }

                break;
        }
        return super.onOptionsItemSelected(item);
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
                    dispatchTakePictureIntent();
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
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
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



    // convert video duration from type Long to type String (mm:ss)


    public ArrayList<Item> getItems() {
        ArrayList<Item> allImages = new ArrayList<Item>(items);
        return allImages;
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
}