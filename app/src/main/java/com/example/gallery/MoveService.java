package com.example.gallery;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.gallery.activity.MainActivity;
import com.example.gallery.activity.ViewItemActivity;
import com.example.gallery.model.Album;
import com.example.gallery.model.Item;

public class MoveService extends Service {
    public MoveService() {
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

                int i = intent.getIntExtra("id_album_choose", -1);
                if (i == -1)
                    return;
                Album album = null;
                album = MainActivity.albums.get(i);
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
                curr.moveTo(album,MoveService.this, volume);
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