package com.example.gallery.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.adapter.AlbumDetailAdapter;
import com.example.gallery.adapter.DateAdapter;
import com.example.gallery.model.Item;

import java.util.ArrayList;

public class ChooseItem extends BaseActivity {
    MenuItem choose;
    ChooseItemApdapter adapter;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseActivity.changeLanguage(MainActivity.language, this);
        BaseActivity.changeTheme(MainActivity.light, this);
        setContentView(R.layout.activity_choose_item);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        adapter = new ChooseItemApdapter(MainActivity.items, this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setAdapter(adapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4 + MainActivity.padding);
        recyclerView.setLayoutManager(gridLayoutManager);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.choose_items_menu, menu);
        return true;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            MainActivity.padding = 3;
            // setContentView(R.layout.yourxmlinlayout-land);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            MainActivity.padding = 0;
        }

        refesh();
    }
    void refesh(){
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4 + MainActivity.padding);
        recyclerView.setLayoutManager(gridLayoutManager);
    }
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
            case R.id.choose:
                if (MainActivity.buffer.size() == 0)
                    break;
                showCopyOrMove();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    void showCopyOrMove(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.choose_copy_move) + "?")

                .setNeutralButton(R.string.cancel , null)
                .setNegativeButton(R.string.copy , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent i1 = new Intent();
                        i1.putExtra("mode", "copy");
                        setResult(RESULT_OK, i1);
                        finish();
                    }
                })
                .setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent i2 = new Intent();
                        i2.putExtra("mode", "move");
                        setResult(RESULT_OK, i2);
                        finish();
                    }
                })
                .create().show();
    }
    private class ChooseItemApdapter  extends RecyclerView.Adapter{
        ArrayList<Item> items = new ArrayList<>();
        private final int IMAGE_TYPE = 0;
        private final int VIDEO_TYPE = 1;
        private Context context;
        ChooseItemApdapter(ArrayList<Item> items, Context context){
            this.items = items;
            this.context = context;
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = null;
            if (viewType== IMAGE_TYPE){
                view = inflater.inflate(R.layout.image, parent, false);
                return new ImageViewHolder(view);
            }
            view = inflater.inflate(R.layout.video_item_layout, parent, false);
            return new VideoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            if (items.get(position).isImage())
            {
                ImageViewHolder imageViewHolder = (ImageViewHolder)holder;

                ImageView imageView = imageViewHolder.imageView;
                Glide.with(context).load(items.get(position).getFilePath()).into(imageView);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                CheckBox check = imageViewHolder.view.findViewById(R.id.check);
                if (MainActivity.buffer.contains(items.get(position)))
                    check.setChecked(true);
                else
                    check.setChecked(false);

                check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            if (!MainActivity.buffer.contains(items.get(position))){
                                {
                                    MainActivity.buffer.add(items.get(position));
                                }

                            }
                            else {
                                if (MainActivity.buffer.contains(items.get(position))){
                                    {
                                        MainActivity.buffer.remove(items.get(position));
                                    }

                                }
                            }
                        }

                    }
                });
                imageViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Do some work here
                        if (MainActivity.buffer.contains(items.get(position))){
                            check.setChecked(false);
                            MainActivity.buffer.remove(items.get(position));

                        }
                        else{
                            check.setChecked(true);
                            if (!MainActivity.buffer.contains(items.get(position)))
                                MainActivity.buffer.add(items.get(position));

                        }
                    }
                });


            }
            else{
                VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
                ImageView thumbnail = videoViewHolder.thumnail;
                Glide.with(context).load(items.get(position).getFilePath()).into(thumbnail);
                thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                TextView textView = videoViewHolder.duration;
                textView.setText(items.get(position).getDuration());
                CheckBox check = videoViewHolder.view.findViewById(R.id.check);

                if (MainActivity.buffer.contains(items.get(position)))
                    check.setChecked(true);
                else
                    check.setChecked(false);

                check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            if (!MainActivity.buffer.contains(items.get(position))){
                                {
                                    MainActivity.buffer.add(items.get(position));
                                }

                            }
                            else {
                                if (MainActivity.buffer.contains(items.get(position))){
                                    {
                                        MainActivity.buffer.remove(items.get(position));
                                    }

                                }
                            }
                        }
                    }
                });
                videoViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Do some work here
                        if (MainActivity.buffer.contains(items.get(position))){
                            check.setChecked(false);
                            MainActivity.buffer.remove(items.get(position));

                        }
                        else{
                            check.setChecked(true);
                            if (!MainActivity.buffer.contains(items.get(position)))
                                MainActivity.buffer.add(items.get(position));

                        }
                    }
                });

            }



        }


        @Override
        public int getItemViewType(int position) {
            return Item.isImageFile((items.get(position).getFilePath())) ? IMAGE_TYPE:VIDEO_TYPE;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

    }
    private class ImageViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        View view;
        public ImageViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imageView = itemView.findViewById(R.id.image);
            CheckBox checkBox = itemView.findViewById(R.id.check);
            checkBox.setVisibility(View.VISIBLE);
        }
    }
    private class VideoViewHolder extends RecyclerView.ViewHolder{
        ImageView thumnail;
        TextView duration;
        View view;
        public VideoViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            thumnail = itemView.findViewById(R.id.thumbnail);
            duration = itemView.findViewById(R.id.text_duration);
            CheckBox checkBox = itemView.findViewById(R.id.check);
            checkBox.setVisibility(View.VISIBLE);
        }
    }
}
