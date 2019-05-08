package com.example.pae_project;

import android.Manifest;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragmentBuilder;


public class IntroInicial extends MaterialIntroActivity {


    @Override protected void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.colorPrimary)
                        .buttonsColor(R.color.colorAccent)
                        .image(R.drawable.logo_locant)
                        .title("Bienvenido a Locant")
                        .description("Mejora tu connexión")
                        .build()
                );

            addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.colorPrimary)
                        .buttonsColor(R.color.colorAccent)
                        .neededPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
                        .image(R.drawable.ic_location_on_black_24dp)
                        .title("Antes de continuar ...")
                        .description("necessitamos revisar los permisos de la aplicación")
                        .build());

        }

        @Override
        public void onFinish() {
            getSharedPreferences("PREFERENCE",MODE_PRIVATE).edit().putBoolean("firstRun",false).commit();
            Intent main = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(main);
        }

}
