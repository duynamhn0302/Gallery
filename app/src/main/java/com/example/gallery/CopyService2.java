package com.example.gallery;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.gallery.activity.MainActivity;
import com.example.gallery.activity.ViewItemActivity;
import com.example.gallery.fragment.Fragment2;
import com.example.gallery.model.Album;
import com.example.gallery.model.Item;

public class CopyService2 extends Service {

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        String name = intent.getStringExtra("name");
        Album a = new Album(name);
        a.setPath(name);
        Item item = (Item) intent.getSerializableExtra("item");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String volume = "external";
                if (MainActivity.curAlbum != null && MainActivity.curAlbum.getType() == Album.typePrivate)
                    volume = "internal";
                item.copyTo(a, CopyService2.this, volume);
                Intent intentDataForMyClient = new Intent("matos.action.GOSERVICE4");
                intentDataForMyClient.putExtra("service4Data", "done!");
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
