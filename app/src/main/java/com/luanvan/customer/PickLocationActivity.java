package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.luanvan.customer.Adapter.RecyclerViewAddressAdapter;
import com.luanvan.customer.Fragments.MapsFragment;
import com.luanvan.customer.Fragments.MapsPickLocationFragment;
import com.luanvan.customer.components.UserLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PickLocationActivity extends AppCompatActivity {

    MaterialToolbar toolbar;
    TextInputEditText etSearch;
    RecyclerView recyclerView;
    List<UserLocation> listUserLocation = new ArrayList<>();
    private MapsPickLocationFragment mapsFragment;
    private SearchLocation task;
    private String currentAddress = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);

        toolbar = findViewById(R.id.toolbarAddress);
        etSearch = findViewById(R.id.etSearch);
        recyclerView = findViewById(R.id.recyclerView);

        showMaps();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        if (!isNetworkConnecting()){
            Toast.makeText(this, getResources().getString(R.string.check_internet_connection), Toast.LENGTH_SHORT).show();
        } else {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (count > 0){
                        if (task != null) task.cancel(true);
                        task = new SearchLocation(PickLocationActivity.this, s.toString());
                        task.execute();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

    }

    private void showMaps(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        mapsFragment = new MapsPickLocationFragment();
        fragmentTransaction.add(R.id.fragment_maps_container, mapsFragment, "MapsPickLocationFragment");
        fragmentTransaction.commit();
    }

    private class SearchLocation extends AsyncTask<List<Address>, String, List<Address>>{
        private Context context;
        private String address;

        public SearchLocation(Context context, String address){
            this.context = context;
            this.address = address;
        }


        @Override
        protected List<Address> doInBackground(List<Address>... lists) {
            Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = null;
            try
            {
                addresses = geoCoder.getFromLocationName(address, 5);

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            super.onPostExecute(addresses);
            if (addresses != null && addresses.size() > 0){
                listUserLocation.clear();
                for (Address address: addresses){
                    double lat = address.getLatitude();
                    double lng = address.getLongitude();
                    currentAddress = address.getFeatureName();
                    listUserLocation.add(new UserLocation(address.getFeatureName(), address.getAddressLine(0), lat, lng));
                }
                Collections.reverse(listUserLocation);
                if (ActivityCompat.checkSelfPermission(PickLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mapsFragment.markLocation(listUserLocation.get(0).getLatitude(), listUserLocation.get(0).getLongitude(), currentAddress);
                }
                recyclerView.setAdapter(new RecyclerViewAddressAdapter(PickLocationActivity.this, listUserLocation));
            }
        }
    }

    public boolean isNetworkConnecting(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        if (networkCapabilities != null) {
            boolean cellular = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            boolean wifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            return wifi || cellular;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}