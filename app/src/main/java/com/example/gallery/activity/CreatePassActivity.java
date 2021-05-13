package com.example.gallery.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.gallery.R;
import com.example.gallery.adapter.AlbumDetailAdapter;
import com.example.gallery.model.Album;

public class CreatePassActivity extends BaseActivity {
    Album album;
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
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseActivity.changeLanguage(MainActivity.language, this);
        BaseActivity.changeTheme(MainActivity.light, this);
        setContentView(R.layout.activiti_create_pass);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        EditText text = findViewById(R.id.pass);
        TextView message = findViewById(R.id.message);

        EditText text2 = findViewById(R.id.repass);

        ImageButton unlock = findViewById(R.id.unlock);
        album = (Album) getIntent().getSerializableExtra("album");
        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = text.getText().toString();
                String repass = text2.getText().toString();
                if (pass.equals(repass)){
                    MainActivity.password = pass;
                    MainActivity.prefsEditor.putString("pass", pass);
                    MainActivity.prefsEditor.apply();
                    setResult(RESULT_OK);
                    finish();
                }
                else
                    message.setText(R.string.khong_khop);

            }
        });

    }
}
