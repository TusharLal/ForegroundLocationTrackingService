package com.example.tusharlal.gpstrackingdemo;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        AppDatabase appDatabase = AppDatabase.getAppDatabase(getApplicationContext());
        LatLng startPoint;
        ArrayList<LatLng> points;
        List<LocationInfo>infos = appDatabase.infoDaoDao().getAll();

        if(infos!=null && infos.size()>2) {
            points = getPathLocations(infos);
        }else{
            points = getDummyLocations();
        }

        PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.width(8);
        lineOptions.color(Color.RED);
        mMap = googleMap;


        lineOptions.addAll(points);
        startPoint = points.get(0);
        mMap.addMarker(new MarkerOptions().position(startPoint).title("Start Point"));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.addPolyline(lineOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 16.0f));
    }

    private ArrayList<LatLng> getDummyLocations(){
        ArrayList<LatLng> points = new ArrayList<LatLng>();
        double[] lats = new double[]{12.965500,12.965655,12.965438,12.965281,12.965093,12.964991
        ,12.965125,12.965321,12.965467,12.965721,12.965870,12.966058,12.966231,12.966461,12.966626};

        double[] lngs = new double[]{77.610769,77.610408,77.610266,77.610145,77.610019,77.609888
        ,77.609773,77.609625,77.609520,77.609327,77.609196,77.609059,77.608930,77.608769,77.60866};

        for (int i=0; i<lats.length; i++){
            points.add(new LatLng(lats[i], lngs[i]));
        }

        return points;
    }

    private ArrayList<LatLng> getPathLocations(List<LocationInfo>infos){
        ArrayList<LatLng> points = new ArrayList<LatLng>();
        for(LocationInfo info:infos){
            points.add(new LatLng(info.getLat(), info.getLng()));
        }
        return points;
    }
}
