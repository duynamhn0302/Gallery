package com.example.gallery.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.activity.InfoItemActivity;
import com.example.gallery.activity.MainActivity;
import com.example.gallery.activity.ViewAlbumActivity;
import com.example.gallery.activity.ViewItemActivity;
import com.example.gallery.model.Album;
import com.example.gallery.model.Image;
import com.example.gallery.model.Item;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.Date;
import java.util.List;

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

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class ScreenSlidePageFragment extends Fragment {
    private Item item;
    public ScreenSlidePageFragment(){

    }
    public ScreenSlidePageFragment(Item item) {
        this.item = item;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (item == null)
            return null;
        if (item.isImage())
            return (ViewGroup) inflater.inflate(R.layout.image_on_slide, container, false);
        return (ViewGroup) inflater.inflate(R.layout.video_on_slide, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        //VideoView videoView = (VideoView) getView().findViewById(R.id.videoOnSlide);
       // if (videoView != null) {
      //      videoView.seekTo(1);
      //  }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Item.isImageFile(item.getFilePath())) {
            PhotoView imageView =  view.findViewById(R.id.imageOnSlide);
            Glide.with(this).load(item.getFilePath()).into(imageView);
            ImageButton edit = view.findViewById(R.id.edit);
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
                    int cur = ViewItemActivity.viewPager.getCurrentItem();
                    File file = new File(MainActivity.items.get(cur).getFilePath());
                    Uri uri;
                    if (file.exists())
                        uri = Uri.fromFile(file);
                    else
                        return;
                    settingsList.getSettingsModel(LoadSettings.class).setSource(uri);

                    settingsList.getSettingsModel(PhotoEditorSaveSettings.class).setOutputToGallery(Environment.DIRECTORY_DCIM);

                    MainActivity.unregist();
                    new EditorBuilder(getActivity())
                            .setSettingsList(settingsList)
                            .startActivityForResult(getActivity(), ViewItemActivity.PESDK_RESULT);

                }
            });

        }else {
            VideoView videoView = (VideoView) view.findViewById(R.id.videoOnSlide);
            MediaController mediaController = new MediaController(getContext());
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);
            videoView.setVideoPath(item.getFilePath());
            videoView.seekTo(1);
        }

        ImageButton info = view.findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), InfoItemActivity.class);
                if(MainActivity.mainMode)
                    intent.putExtra("info", MainActivity.items.get(ViewItemActivity.viewPager.getCurrentItem()));
                else
                    intent.putExtra("info", MainActivity.curAlbum.getImages().get(ViewItemActivity.viewPager.getCurrentItem()));

                startActivity(intent);
            }
        });

        ImageButton share = view.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = MainActivity.items.get(ViewItemActivity.viewPager.getCurrentItem());
                File fileToShare = new File(item.getFilePath());
                if (fileToShare.exists()) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri uri = FileProvider.getUriForFile(getContext(),
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
        ImageButton del = view.findViewById(R.id.del);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
        ImageButton love = view.findViewById(R.id.love);
        if(item.isLoved())
            love.setImageResource(R.drawable.ic_baseline_favorite_red_24);
        if (MainActivity.privateAlbum.getImages().contains(item))
            love.setEnabled(false);
        love.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                Item curr = item;
                if(!curr.isLoved()){

                    love.setImageResource(R.drawable.ic_baseline_favorite_red_24);
                    curr.setLoved(true);
                    MainActivity.listLove.add(curr.getFilePath());
                    MainActivity.loveAlbum.addItem(curr);
                }
                else{
                    curr.setLoved(false);

                    love.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    for(String s: MainActivity.listLove)
                        if (s.equals(curr.getFilePath())){
                            MainActivity.listLove.remove(s);
                            break;
                        }
                    if (MainActivity.mainMode)
                        MainActivity.loveAlbum.getImages().remove(curr);
                    else
                        if ( MainActivity.mainMode == false && MainActivity.curAlbum.getType() == Album.typeLove)
                        {
                            //ViewAlbumActivity.refesh();
                          //  ViewItemActivity.viewPager.setAdapter(ViewItemActivity.adapter);
                         //   ViewItemActivity.viewPager.setCurrentItem(0);
                            Item itemDel = ViewItemActivity.adapter.remove(ViewItemActivity.viewPager.getCurrentItem());
                            ViewItemActivity.viewPager.setAdapter(ViewItemActivity.adapter);
                            ViewItemActivity.viewPager.setCurrentItem(MainActivity.items.indexOf(ViewItemActivity.adapter.currentItem));
                            MainActivity.loveAlbum.getImages().remove(curr);
                            ViewAlbumActivity.refesh();
                        }

                }
                MainActivity.saveArrayList(MainActivity.listLove, "listLove");
            }
        });

    }


    public void deletePhoto()  {
        MainActivity.unregist();
        Item itemDel = ViewItemActivity.adapter.remove(ViewItemActivity.viewPager.getCurrentItem());
        itemDel.delete(getContext(), false);
        ViewItemActivity.viewPager.setAdapter(ViewItemActivity.adapter);
        ViewItemActivity.viewPager.setCurrentItem(MainActivity.items.indexOf(ViewItemActivity.adapter.currentItem));
        MainActivity.regist();
    }

    public void showDeleteDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.delete_item )+ "?")
                .setNegativeButton(getString(R.string.no), null)
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deletePhoto();
                    }
                })
                .create().show();
    }
}
