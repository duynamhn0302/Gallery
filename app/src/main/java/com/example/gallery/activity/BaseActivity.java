package com.example.gallery.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gallery.R;

import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {
    static public  String language = "vi";
    static public boolean light = true;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = getBaseContext();
        changeLanguage(MainActivity.language, context);
        changeTheme(MainActivity.light, context);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    static  public void changeLanguage (String language, Context context){

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        context.getResources().updateConfiguration(configuration,
                context.getResources().getDisplayMetrics());
    }
    static public void changeTheme(boolean light, Context context){
        if (light) {
            context.setTheme(R.style.LightTheme);
        } else {
            context.setTheme(R.style.DarkTheme);
        }
    }

    public  void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }
}