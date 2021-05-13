package com.example.gallery.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.exifinterface.media.ExifInterface;
import androidx.loader.content.CursorLoader;

import com.example.gallery.R;
import com.example.gallery.model.Album;
import com.example.gallery.model.Image;
import com.example.gallery.model.Item;
import com.example.gallery.model.Video;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.places.common.PlaceConstants;
import com.shivtechs.maplocationpicker.LocationPickerActivity;
import com.shivtechs.maplocationpicker.MapUtility;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

public class InfoItemActivity extends BaseActivity{
    TextView added_date;
    TextView item_name;
    TextView specification;
    TextView path;
    TextView location;
    TextView location_label;
    Geocoder geocoder;
    Item item;
    ExifInterface exif = null;
    private static final int ADDRESS_PICKER_REQUEST = 1020;
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
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseActivity.changeLanguage(MainActivity.language, this);
        BaseActivity.changeTheme(MainActivity.light, this);
        setContentView(R.layout.activity_info_item);
        MapUtility.apiKey = getResources().getString(R.string.your_api_key);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        item = (Item) getIntent().getSerializableExtra("info");

        added_date = (TextView) findViewById(R.id.added_date);
        item_name = (TextView) findViewById(R.id.item_name);
        specification = (TextView) findViewById(R.id.specification);
        path = (TextView) findViewById(R.id.path);
        location = (TextView) findViewById(R.id.location);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(InfoItemActivity.this, MapsActivity.class);
                startActivityForResult(i, ADDRESS_PICKER_REQUEST);
            }
        });
        location_label = findViewById(R.id.location_label);


        try {
            exif = new ExifInterface(item.getFilePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        double[] latLng = new double[2];
        latLng = exif.getLatLong();

        //Read location details
        geocoder = new Geocoder(this);
        List<Address> addresses = null;
        String address = "";
        try {
            if (latLng != null) {
                addresses = geocoder.getFromLocation(latLng[0], latLng[1], 1);
                if (addresses.size() > 0 && addresses != null) {
                    address = addresses.get(0).getAddressLine(0);
                    location.setText(address);
                }
            }
            else {
                location.setText("none");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Read image resolution
        if (item.isImage()) {
            Bitmap bitmap = BitmapFactory.decodeFile(item.getFilePath());
            int image_height = bitmap.getHeight();
            int image_width = bitmap.getWidth();
            String resolution = String.valueOf(image_width) + "x" + String.valueOf(image_height);
            specification = findViewById(R.id.specification);
            specification.setText(resolution);
        }
        else {
            //Read video resolution
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(item.getFilePath());
            String video_height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String video_width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String resolution = video_width + "x" + video_height;
            specification = findViewById(R.id.specification);
            specification.setText(resolution);
        }

        //Read file added date
        added_date.setText(item.getAddedDate());

        //Read file name, path
        File file = new File(item.getFilePath());
        String strFileName = file.getName();
        String strFilePath = file.getParent();
        item_name.setText(strFileName);
        path.setText(strFilePath);

        //Read file length
        DecimalFormat dec = new DecimalFormat("0.00");
        String fileLengthToString = dec.format(file.length()/1024.0/1024.0).concat("MB");
        specification.setText(specification.getText() + getString(R.string.tab)  + fileLengthToString + getString(R.string.end_line));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADDRESS_PICKER_REQUEST && resultCode == RESULT_OK) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.save_location)
                    .setNegativeButton(getString(R.string.no), null)
                    .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            update(data);
                        }
                    })
                    .create().show();


        }
    }
    void update(Intent data){
        CameraPosition cameraPosition = data.getParcelableExtra(PlaceConstants.MAP_CAMERA_POSITION);
        LatLng latLng = cameraPosition.target;
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0 && addresses != null) {
            String address = addresses.get(0).getAddressLine(0);
            location.setText(address);
            exif.setLatLong(latLng.getLatitude(), latLng.getLongitude());
            try {

                exif.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
