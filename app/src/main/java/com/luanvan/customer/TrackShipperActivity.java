package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.luanvan.customer.Fragments.MapsFragment;

public class TrackShipperActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_shipper);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            showMaps();
//        } else if (shouldShowRequestPermissionRationale("android.permission.ACCESS_FINE_LOCATION")) {
//            showInContextUI(this);
        } else {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_CODE);
        }
    }

    private void showInContextUI(Context context){
        Toast.makeText(context, "shouldShowRequestPermissionRationale", Toast.LENGTH_LONG).show();
    }

    private void showMaps(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        MapsFragment mapsFragment = new MapsFragment();
        fragmentTransaction.add(R.id.fragment_maps_container, mapsFragment);
        fragmentTransaction.commit();
    }
}