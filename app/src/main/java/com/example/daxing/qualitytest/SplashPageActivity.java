package com.example.daxing.qualitytest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class SplashPageActivity extends android.app.Activity {
    private ImageView splash_logo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_page);
        splash_logo = (ImageView) findViewById(R.id.splash_photo);
        splash_logo.setImageResource(R.drawable.youtube_logo);
        Log.i("SplashPage", "Add splash photo");
        Thread welcomeThread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run();
                    sleep(2000);  //Delay of 2 seconds
                } catch (Exception e) {

                } finally {

                    Intent i = new Intent(SplashPageActivity.this,
                            LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        welcomeThread.start();
    }
}
