package com.example.tusharlal.gpstrackingdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_COARSE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        Button startButton = findViewById(R.id.button1);
        Button stopButton = findViewById(R.id.button2);
        Button showMap = findViewById(R.id.button3);

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        showMap.setOnClickListener(this);
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
                startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(startIntent);
                } else {
                    startService(startIntent);
                }
                break;
            case R.id.button2:
                Intent stopIntent = new Intent(MainActivity.this, ForegroundService.class);
                stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(stopIntent);
                } else {
                    startService(stopIntent);
                }
                break;

            case R.id.button3:
                AppDatabase appDatabase = AppDatabase.getAppDatabase(getApplicationContext());

                String msg = "Map Activity";
                if(appDatabase.infoDaoDao().getAll()!=null){
                    List<LocationInfo> infos = appDatabase.infoDaoDao().getAll();
                    for (LocationInfo info: infos) {
                        msg = info.getLat()+", "+ info.getLng()+" /n";
                    }
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }
}
