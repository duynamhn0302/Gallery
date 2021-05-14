package com.example.gallery;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

import com.example.gallery.activity.MainActivity;
import com.example.gallery.activity.ViewItemActivity;
import com.example.gallery.model.Album;
import com.example.gallery.model.Item;

public class MoveService extends Service {
    public MoveService() {
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
                String name = intent.getStringExtra("path");
                Album album = new Album(name);
                album.setPath(name);
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
                if (curr == null)
                    return;
                curr.copyTo(album, MoveService.this,volume);
                curr.delete(MoveService.this, MainActivity.privateAlbum.getImages().contains(curr));
                Intent intentDataForMyClient = new Intent("matos.action.GOSERVICE3");
                intentDataForMyClient.putExtra("service3Data", "done!");
                sendBroadcast(intentDataForMyClient);
                stopSelf();


        stopSelf();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}