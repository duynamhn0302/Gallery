package com.example.gallery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.os.ResultReceiver;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.gallery.activity.MainActivity;
import com.example.gallery.activity.ViewItemActivity;
import com.example.gallery.model.Album;
import com.example.gallery.model.Item;

public class CopyService extends Service {



    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Item curr = null;
                if(MainActivity.mainMode){

                    curr = MainActivity.items.get(ViewItemActivity.viewPager.getCurrentItem());
                }
                else {
                    curr = MainActivity.curAlbum.getImages().get(ViewItemActivity.viewPager.getCurrentItem());
                }
                String volume = "external";
                if (MainActivity.curAlbum != null && MainActivity.curAlbum.getType() == Album.typePrivate)
                    volume = "internal";
                String name = curr.copy(CopyService.this, volume);
                Intent intentDataForMyClient = new Intent("matos.action.GOSERVICE3");
                intentDataForMyClient.putExtra("service3Data", name);
                sendBroadcast(intentDataForMyClient);
                stopSelf();
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
