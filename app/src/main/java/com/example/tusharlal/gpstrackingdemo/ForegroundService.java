package com.example.tusharlal.gpstrackingdemo;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ForegroundService extends Service implements LocationListener{
    private static final String LOG_TAG = "ForegroundService";

//    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 3; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    // Location storage
    private List<Location> locationList;

    private AppDatabase appDatabase;

//    public ForegroundService(Context context){
//        this.mContext = context;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        return null;
    }
    String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");
            locationList = new ArrayList<>();
            appDatabase = AppDatabase.getAppDatabase(getApplicationContext());
            if(appDatabase.infoDaoDao().getAll()!=null)
                appDatabase.infoDaoDao().deleteAll();
            getLocation();
//            Intent notificationIntent = new Intent(this, MainActivity.class);
//            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
//            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//
//            Intent previousIntent = new Intent(this, ForegroundService.class);
//            previousIntent.setAction(Constants.ACTION.PREV_ACTION);
//            PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);
//
//            Intent playIntent = new Intent(this, ForegroundService.class);
//            playIntent.setAction(Constants.ACTION.PLAY_ACTION);
//            PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);
//
//            Intent nextIntent = new Intent(this, ForegroundService.class);
//            nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
//            PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

//            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_round);

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//                Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
//                        .setContentTitle("Truiton Music Player")
//                        .setTicker("Truiton Music Player")
//                        .setContentText("My Music")
//                        .setSmallIcon(R.drawable.ic_launcher)
////                        .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
//                        .setContentIntent(pendingIntent)
//                        .setOngoing(true)
//                        .addAction(android.R.drawable.ic_media_previous, "Previous", ppreviousIntent)
//                        .addAction(android.R.drawable.ic_media_play, "Play", pplayIntent)
//                        .addAction(android.R.drawable.ic_media_next, "Next", pnextIntent).build();
//                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
//            }else{
//                Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//                        .setContentTitle("Truiton Music Player")
//                        .setTicker("Truiton Music Player")
//                        .setContentText("My Music")
//                        .setSmallIcon(R.drawable.ic_launcher)
////                        .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
//                        .setContentIntent(pendingIntent)
//                        .setOngoing(true)
//                        .addAction(android.R.drawable.ic_media_previous, "Previous", ppreviousIntent)
//                        .addAction(android.R.drawable.ic_media_play, "Play", pplayIntent)
//                        .addAction(android.R.drawable.ic_media_next, "Next", pnextIntent).build();
                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, createNotificationChannel());
//            }
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)
                || intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopUsingGPS();
            stopForeground(true);
            stopSelf();
        }

//        else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
//            Log.i(LOG_TAG, "Clicked Previous");
//        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
//            Log.i(LOG_TAG, "Clicked Next");
//        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
//            Log.i(LOG_TAG, "Received Stop Foreground Intent");
//            stopUsingGPS();
//            stopForeground(true);
//            stopSelf();
//        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            AppDatabase.destroyInstance();
            stopUsingGPS();
            super.onDestroy();
            Log.i(LOG_TAG, "In onDestroy");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
//        Log.i(LOG_TAG, "Lat: "+location.getLatitude()
//                +"/nLng: "+location.getLongitude()+"/nSpd: "+location.getSpeed());
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //deprecated in API 26
            v.vibrate(500);
        }
        Toast.makeText(this, "Lat: "+location.getLatitude()
                +"/nLng: "+location.getLongitude()+"/nSpd: "+location.getSpeed()
                , Toast.LENGTH_SHORT).show();
//        locationList.add(location);
        try{
            LocationInfo info = new LocationInfo();
            info.setLat(location.getLatitude());
            info.setLng(location.getLongitude());
            info.setSpeed(location.getSpeed());
            info.setTime(location.getTime());

            if(appDatabase==null)
                appDatabase = AppDatabase.getAppDatabase(getApplicationContext());

            appDatabase.infoDaoDao().insertInfo(info);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                // First get location from Network Provider
//                if (isNetworkEnabled) {
//                    locationManager.requestLocationUpdates(
//                            LocationManager.NETWORK_PROVIDER,
//                            MIN_TIME_BW_UPDATES,
//                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//                    Log.d("Network", "Network");
//                    if (locationManager != null) {
//                        location = locationManager
//                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                        if (location != null) {
//                            latitude = location.getLatitude();
//                            longitude = location.getLongitude();
//                        }
//                    }
//                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            Log.i(LOG_TAG, "Lat: "+location.getLatitude()
                                    +"/nLng: "+location.getLongitude()+"/nSpd: "+location.getSpeed());
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                            try{
                                LocationInfo info = new LocationInfo();
                                info.setLat(location.getLatitude());
                                info.setLng(location.getLongitude());
                                info.setSpeed(location.getSpeed());
                                info.setTime(location.getTime());

                                if(appDatabase==null)
                                    appDatabase = AppDatabase.getAppDatabase(getApplicationContext());

                                appDatabase.infoDaoDao().insertInfo(info);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(this);
        }
    }

    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    public List<Location> getLocationList(){
        return locationList;
    }

    private Notification createNotificationChannel() {

        Intent playIntent = new Intent(this, ForegroundService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID
                    , "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(android.R.color.holo_red_dark);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setTicker("GPSTracker")
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("Default notification")
                .setContentText("Your location is being tracked.")
                .setContentInfo("Info")
                .addAction(android.R.drawable.ic_media_play, "Stop Service", pplayIntent);

//        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
        return notificationBuilder.build();
    }

}
