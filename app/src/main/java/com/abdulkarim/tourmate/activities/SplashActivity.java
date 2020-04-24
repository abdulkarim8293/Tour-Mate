package com.abdulkarim.tourmate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.helper.GpsProvider;
import com.abdulkarim.tourmate.helper.SharedPref;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private GpsProvider gpsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        init();
    }

    private void init() {
        sharedPreferences = getSharedPreferences(SharedPref.SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gpsProvider = new GpsProvider(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (gpsProvider.statusCheck() == true) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (sharedPreferences.getInt(SharedPref.USER_ID, 0) == 0) {
                        Intent intent = new Intent(SplashActivity.this, SignUpActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                }
            }, 2000);
        }else {
            gpsProvider.showDialogForLocation();
        }
    }
}
