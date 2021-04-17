package com.example.gallery.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.activity.MainActivity;
import com.example.gallery.model.Item;
import com.github.chrisbanes.photoview.PhotoView;

public class ScreenSlidePageFragment extends Fragment {
    private Item item;

    public ScreenSlidePageFragment(Item item) {
        this.item = item;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (item.getIsImage())
            return (ViewGroup) inflater.inflate(R.layout.image_on_slide, container, false);
        return (ViewGroup) inflater.inflate(R.layout.video_on_slide, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        VideoView videoView = (VideoView) getView().findViewById(R.id.videoOnSlide);
        if (videoView != null) {
            videoView.seekTo(1);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (MainActivity.isImageFile(item.getFilePath())) {
            PhotoView imageView =  view.findViewById(R.id.imageOnSlide);
            Glide.with(this).load(item.getFilePath()).into(imageView);
        }else {
            VideoView videoView = (VideoView) view.findViewById(R.id.videoOnSlide);
            MediaController mediaController = new MediaController(getContext());
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);
            videoView.setVideoPath(item.getFilePath());
            videoView.seekTo(1);
        }
    }
}
