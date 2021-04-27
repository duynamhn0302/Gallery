package com.example.gallery.activity;

import android.Manifest;
import android.app.RecoverableSecurityException;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.loader.content.CursorLoader;
import androidx.viewpager2.widget.ViewPager2;

import com.davemorrissey.labs.subscaleview.BuildConfig;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.example.gallery.R;
import com.example.gallery.adapter.ScreenSlidePagerAdapter;
import com.example.gallery.model.Image;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.gallery.model.Item;
import com.example.gallery.model.Video;

import ly.img.android.pesdk.PhotoEditorSettingsList;
import ly.img.android.pesdk.assets.filter.basic.FilterPackBasic;
import ly.img.android.pesdk.assets.font.basic.FontPackBasic;
import ly.img.android.pesdk.assets.frame.basic.FramePackBasic;
import ly.img.android.pesdk.assets.overlay.basic.OverlayPackBasic;
import ly.img.android.pesdk.assets.sticker.emoticons.StickerPackEmoticons;
import ly.img.android.pesdk.assets.sticker.shapes.StickerPackShapes;
import ly.img.android.pesdk.backend.model.EditorSDKResult;
import ly.img.android.pesdk.backend.model.state.LoadSettings;
import ly.img.android.pesdk.backend.model.state.PhotoEditorSaveSettings;
import ly.img.android.pesdk.backend.model.state.manager.SettingsList;
import ly.img.android.pesdk.ui.activity.EditorBuilder;
import ly.img.android.pesdk.ui.model.state.UiConfigFilter;
import ly.img.android.pesdk.ui.model.state.UiConfigFrame;
import ly.img.android.pesdk.ui.model.state.UiConfigOverlay;
import ly.img.android.pesdk.ui.model.state.UiConfigSticker;
import ly.img.android.pesdk.ui.model.state.UiConfigText;
import ly.img.android.serializer._3.IMGLYFileWriter;


public class ViewItemActivity extends AppCompatActivity {
    final int PESDK_RESULT = 1;
    private ViewPager2 viewPager;
    Item item;
    private ImageView imageView;
    private int image_request_code = 100;
    ScreenSlidePagerAdapter adapter;
    private final int  DS_PHOTO_EDITOR_REQUEST_CODE = 555;
    public static final String OUTPUT_PHOTO_DIRECTORY = "gallery_edit";
    private static final int UPDATED_CODE = 222;
    private boolean change = false;
    public void deletePhoto()  {
        change = true;
        Item itemDel = adapter.remove(viewPager.getCurrentItem());
        MainActivity.items.remove(itemDel);
        File file = new File(itemDel.getFilePath());
        boolean success = file.delete();
        callBroadCast();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(MainActivity.items.indexOf(adapter.currentItem));

    }

    @Override
    public void onBackPressed() {
        int res = change?RESULT_OK:RESULT_CANCELED;
        setResult(res);
        finish();
    }

    public void callBroadCast() {
        if (Build.VERSION.SDK_INT >= 14) {
            Log.e("-->", " >= 14");
            MediaScannerConnection.scanFile(this, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {

                public void onScanCompleted(String path, Uri uri) {
                    Log.e("ExternalStorage", "Scanned " + path + ":");
                    Log.e("ExternalStorage", "-> uri=" + uri);
                }
            });
        } else {
            Log.e("-->", " < 14");
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }
    public void showDeleteDialog() {
        new AlertDialog.Builder(this, R.style.Widget_MaterialComponents_ActionBar_Solid)
                .setTitle(R.string.delete_item + "?")
                .setNegativeButton(getString(R.string.no), null)
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deletePhoto();
                    }
                })
                .create().show();
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        item = (Item) getIntent().getSerializableExtra("item");
        viewPager = findViewById(R.id.imagesSlider);
        adapter = createScreenSlideAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(MainActivity.items.indexOf(item), false);
        ImageButton edit = findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a empty new SettingsList and apply the changes on this referance.
                PhotoEditorSettingsList settingsList = new PhotoEditorSettingsList();

                // If you include our asset Packs and you use our UI you also need to add them to the UI,
                // otherwise they are only available for the backend
                // See the specific feature sections of our guides if you want to know how to add our own Assets.

                settingsList.getSettingsModel(UiConfigFilter.class).setFilterList(
                        FilterPackBasic.getFilterPack()
                );

                settingsList.getSettingsModel(UiConfigText.class).setFontList(
                        FontPackBasic.getFontPack()
                );

                settingsList.getSettingsModel(UiConfigFrame.class).setFrameList(
                        FramePackBasic.getFramePack()
                );

                settingsList.getSettingsModel(UiConfigOverlay.class).setOverlayList(
                        OverlayPackBasic.getOverlayPack()
                );

                settingsList.getSettingsModel(UiConfigSticker.class).setStickerLists(
                        StickerPackEmoticons.getStickerCategory(),
                        StickerPackShapes.getStickerCategory()
                );
                int cur = viewPager.getCurrentItem();
                File file = new File(MainActivity.items.get(cur).getFilePath());
                Uri uri;
                if (file.exists())
                    uri = Uri.fromFile(file);
                else
                    return;
                settingsList.getSettingsModel(LoadSettings.class).setSource(uri);

                settingsList.getSettingsModel(PhotoEditorSaveSettings.class).setOutputToGallery(Environment.DIRECTORY_DCIM);

                new EditorBuilder(ViewItemActivity.this)
                        .setSettingsList(settingsList)
                        .startActivityForResult(ViewItemActivity.this, PESDK_RESULT);

            }
        });
        ImageButton info = findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewItemActivity.this, InfoItemActivity.class);
                intent.putExtra("info", MainActivity.items.get(viewPager.getCurrentItem()));
                startActivity(intent);
            }
        });

        ImageButton share = findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = MainActivity.items.get(viewPager.getCurrentItem());
                File fileToShare = new File(item.getFilePath());
                if (fileToShare.exists()) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri uri = FileProvider.getUriForFile(ViewItemActivity.this,
                            "com.example.gallery.provider", fileToShare);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    if (item.isImage())
                        intent.setType("image/*");
                    else
                        intent.setType("video/*");
                    startActivity(Intent.createChooser(intent, "Share file using"));
                }
            }
        });
        ImageButton del = findViewById(R.id.del);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
    }

    private ScreenSlidePagerAdapter createScreenSlideAdapter() {
        ScreenSlidePagerAdapter adapter = new ScreenSlidePagerAdapter(this,  MainActivity.items, item);
        return adapter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PESDK_RESULT) {
            // Editor has saved an Image.
            change = true;
            EditorSDKResult dataa = new EditorSDKResult(data);

            dataa.notifyGallery(EditorSDKResult.UPDATE_RESULT & EditorSDKResult.UPDATE_SOURCE);
            Uri uriSource = dataa.getSourceUri();
            Uri res = dataa.getResultUri();
            Log.i("PESDK", "Source image is located here " + uriSource);
            Log.i("PESDK", "Result image is located here " + res);

            // TODO: Do something with the result image

            // OPTIONAL: read the latest state to save it as a serialisation
            SettingsList lastState = dataa.getSettingsList();
            File newFile = new File(
                    Environment.getExternalStorageDirectory(),
                    "serialisationReadyToReadWithPESDKFileReader.json"
            );
            try {

                new IMGLYFileWriter(lastState).writeJson(newFile);
            } catch (Exception e) { e.printStackTrace(); }

            item = newItem();
            MainActivity.items.add(0, item);
            adapter.notifyDataSetChanged();
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(0);
        } else if (resultCode == RESULT_CANCELED && requestCode == PESDK_RESULT) {
            // Editor was canceled
            EditorSDKResult dataa = new EditorSDKResult(data);

            Uri sourceURI = dataa.getSourceUri();
            // TODO: Do something...
        }


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

                item = new Image(id, absolutePathOfFile,  addedDate);
            }
            catch (Exception ex){
                System.out.println(absolutePathOfFile);
            }
        }
        cursor.close();
        return item;
    }


}
