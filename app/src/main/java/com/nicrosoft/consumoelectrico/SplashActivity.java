package com.nicrosoft.consumoelectrico;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.nicrosoft.consumoelectrico.activities.Main;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        finish();
    }
}
