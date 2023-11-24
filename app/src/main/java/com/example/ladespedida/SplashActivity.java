package com.example.ladespedida;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;


public class SplashActivity extends sinBarraSuperior {
    Object window;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);


    };
}



