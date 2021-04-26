package com.example.gallery.adapter;

import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.gallery.fragment.ScreenSlidePageFragment;
import com.example.gallery.model.Item;

import java.io.File;
import java.util.ArrayList;

public class ScreenSlidePagerAdapter extends FragmentStateAdapter {
    private Item currentItem;
    private ArrayList<Item> items;
    public ScreenSlidePagerAdapter(FragmentActivity fa, ArrayList<Item> items, Item currentItem) {
        super(fa);
        this.items = items;
        this.currentItem = currentItem;
    }

    @Override
    public Fragment createFragment(int position) {
        return new ScreenSlidePageFragment(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    public Item remove(int pos){
        Item preItem = items.get(pos);
        int curIndex = items.indexOf(currentItem);
        if (curIndex + 1 >= items.size())
            if(curIndex-1 >= 0){
                currentItem = items.get(curIndex-1);
            }
            else {
                currentItem = null;
            }
        else {
            currentItem = items.get(curIndex+1);
        }
        items.remove(preItem);
        return preItem;
    }
}
