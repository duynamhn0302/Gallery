package com.example.gallery.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.gallery.R;

import java.util.ArrayList;
import java.util.Locale;

public class SettingsActivity extends BaseActivity {
    String language = "";
    boolean light = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseActivity.changeLanguage(MainActivity.language, this);
        BaseActivity.changeTheme(MainActivity.light, this);
        setContentView(R.layout.settings_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RadioButton vi = findViewById(R.id.vn);
        RadioButton en = findViewById(R.id.en);
        RadioButton light = findViewById(R.id.light);
        RadioButton dark = findViewById(R.id.dark);
        if (MainActivity.language.equals("vi"))
            vi.setChecked(true);
        else
            en.setChecked(true);
        if (MainActivity.light == true)
            light.setChecked(true);
        else
            dark.setChecked(true);
        Button b = findViewById(R.id.save);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String l;
                boolean t;
                boolean change = false;
                if (vi.isChecked())
                    l = "vi";
                else
                    l = "en";
                if (light.isChecked())
                    t = true;
                else
                    t = false;
                if (!l.equals(MainActivity.light)){
                    MainActivity.language = l;
                    MainActivity.prefsEditor.putString("language", l);
                    MainActivity.prefsEditor.apply();
                    change  = true;
                }
                if (t != MainActivity.light){
                    MainActivity.light = t;
                    MainActivity.prefsEditor.putBoolean("theme", t);
                    MainActivity.prefsEditor.apply();
                    change  = true;
                }
                if (change)
                {
                    setResult(RESULT_OK);
                    finish();
                }
                else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        });

    }
    void changeLanguage (String language){

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        this.getResources().updateConfiguration(configuration,
                this.getResources().getDisplayMetrics());
    }
    void changeTheme(boolean light){
        if (light) {
            this.setTheme(R.style.LightTheme);
        } else {
            this.setTheme(R.style.DarkTheme);
        }
    }

    void save(){

        MainActivity.prefsEditor.putBoolean("light", light);
        MainActivity.prefsEditor.apply();
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
        }
        return super.onOptionsItemSelected(item);
    }

}
