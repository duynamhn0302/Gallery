package com.example.gallery.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.exifinterface.media.ExifInterface;

import com.example.gallery.R;
import com.example.gallery.model.Item;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class InfoItemActivity extends AppCompatActivity {
    TextView added_date;
    TextView item_name;
    TextView specification;
    TextView path;
    TextView location;
    TextView location_label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_item);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Item item = (Item) getIntent().getSerializableExtra("info");

        added_date = (TextView) findViewById(R.id.added_date);
        item_name = (TextView) findViewById(R.id.item_name);
        specification = (TextView) findViewById(R.id.specification);
        path = (TextView) findViewById(R.id.path);
        location = (TextView) findViewById(R.id.location);
        location_label = findViewById(R.id.location_label);

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(item.getFilePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        double[] latLng = new double[2];
        latLng = exif.getLatLong();

        //Read location details
        Geocoder geocoder = new Geocoder(this);
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
                location_label.setVisibility(View.GONE);
                location.setVisibility(View.GONE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Read image resolution
        if (item.getIsImage()) {
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
}
