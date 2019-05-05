package com.example.pae_project;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragmentBuilder;


public class Intro extends MaterialIntroActivity {
        ImageView image;


    @Override protected void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            Boolean firstRun = getSharedPreferences("PREFERENCE",MODE_PRIVATE).getBoolean("firstRun",true);
            long starttime = System.currentTimeMillis() ;
            setContentView(R.layout.activity_logo);

        if(!firstRun) {

            addSlide(new SlideFragmentBuilder()
                    .backgroundColor(R.color.colorPrimaryDark)
                    .buttonsColor(R.color.colorAccent)
                    .image(R.drawable.logo_locant)
                    .description("Millora la teva connexió amb Location")
                    .build()
            );

                Log.i("Intro", "not firstRun");
                while((System.currentTimeMillis() - starttime) > 100000 ){
                    Log.i("Intro", "estic dintre el bucle");
                }
                Intent main = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(main);
            }
            else {
                addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.colorPrimaryDark)
                        .buttonsColor(R.color.colorAccent)
                        .image(R.drawable.logo_app)
                        .title("Benvinguts a Location")
                        .description("Millora la teva connexió amb Location")
                        .build()
                );

                addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.colorPrimary)
                        .buttonsColor(R.color.colorAccent)
                        .neededPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
                        .image(R.drawable.ic_location_on_black_24dp)
                        .title("Abans de continuar ...")
                        .description("necessitem revisar els permisos de l'aplicació")
                        .build());
            }

        }

        @Override
        public void onFinish() {
            getSharedPreferences("PREFERENCE",MODE_PRIVATE).edit().putBoolean("firstRun",false).commit();
            Intent main = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(main);
        }

}
