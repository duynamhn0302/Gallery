package com.example.gallery.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.gallery.fragment.ScreenSlidePageFragment;
import com.example.gallery.model.Item;

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
}
