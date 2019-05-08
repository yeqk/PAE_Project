package com.example.pae_project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class IntroLogo extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("intro","ESTOY en el on create");

        Boolean firstRun = getSharedPreferences("PREFERENCE",MODE_PRIVATE).getBoolean("firstRun",true);
        if(!firstRun) {
            setContentView(R.layout.activity_logo);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override public void run() {
                    startActivity(new Intent(IntroLogo.this, MainActivity.class));
                }
            },2000);

        }
        else{
            startActivity(new Intent(IntroLogo.this, IntroInicial.class));
        }
    }
}
