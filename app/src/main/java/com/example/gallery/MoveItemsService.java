package com.example.gallery;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.gallery.activity.MainActivity;
import com.example.gallery.activity.ViewItemActivity;
import com.example.gallery.fragment.Fragment2;
import com.example.gallery.model.Album;
import com.example.gallery.model.Item;

public class MoveItemsService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String name = intent.getStringExtra("path");
        Album a = new Album(name);
        a.setPath(name);

        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                int count = 1;
                for(Item item:MainActivity.buffer) {
                    String volume = "external";
                    if (MainActivity.curAlbum != null && MainActivity.curAlbum.getType() == Album.typePrivate)
                        volume = "internal";
                    item.copyTo(a, MoveItemsService.this, volume);
                    Intent intentDataForMyClient = new Intent("matos.action.GOSERVICE5");
                    intentDataForMyClient.putExtra("number", count);
                    sendBroadcast(intentDataForMyClient);
                    count++;
                }
                stopSelf();
            }
        }).start();
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e ("<<My>>", "I am Dead-3");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

