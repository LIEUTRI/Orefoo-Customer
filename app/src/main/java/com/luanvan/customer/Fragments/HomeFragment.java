package com.luanvan.customer.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.luanvan.customer.PickLocationActivity;
import com.luanvan.customer.R;
import com.luanvan.customer.RestaurantActivity;
import com.luanvan.customer.SearchActivity;
import com.luanvan.customer.components.Branch;
import com.luanvan.customer.components.CartDialog;
import com.luanvan.customer.components.RequestUrl;
import com.luanvan.customer.components.RequestsCode;
import com.luanvan.customer.components.ResultsCode;
import com.luanvan.customer.components.Shared;
import com.luanvan.customer.components.SortPlaces;

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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private TextView tvSearch;
    private TextView tvAddress;
    private FloatingActionButton btnCart;
    private TextView tvSizeOfCart;

    private MaterialCardView cvItem1, cvItem2, cvItem3, cvItem4, cvItem5;
    private TextView tvItemName1, tvItemName2, tvItemName3, tvItemName4, tvItemName5;
    private TextView tvKm1, tvKm2, tvKm3, tvKm4, tvKm5;
    private ImageView ivItem1, ivItem2, ivItem3, ivItem4, ivItem5;

    private FusedLocationProviderClient fusedLocationClient;
    private int consumerID = -1;
    private int userId;
    private String username;
    private String consumerLocation = "";
    private String token = "";
    private int cartId;
    private ArrayList<Branch> branches = new ArrayList<>();

    public static LatLng currentLocation;
    private String addressLine = "";

    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;

    public HomeFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Shared.CART, Context.MODE_PRIVATE);
        cartId = sharedPreferences.getInt(Shared.KEY_CART_ID, -1);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvSearch = view.findViewById(R.id.tvSearch);
        tvAddress = view.findViewById(R.id.tvAddress);
        ivItem1 = view.findViewById(R.id.ivItem1);
        ivItem2 = view.findViewById(R.id.ivItem2);
        ivItem3 = view.findViewById(R.id.ivItem3);
        ivItem4 = view.findViewById(R.id.ivItem4);
        ivItem5 = view.findViewById(R.id.ivItem5);
        tvItemName1 = view.findViewById(R.id.tvItemName1);
        tvItemName2 = view.findViewById(R.id.tvItemName2);
        tvItemName3 = view.findViewById(R.id.tvItemName3);
        tvItemName4 = view.findViewById(R.id.tvItemName4);
        tvItemName5 = view.findViewById(R.id.tvItemName5);
        tvKm1 = view.findViewById(R.id.tvItemKm1);
        tvKm2 = view.findViewById(R.id.tvItemKm2);
        tvKm3 = view.findViewById(R.id.tvItemKm3);
        tvKm4 = view.findViewById(R.id.tvItemKm4);
        tvKm5 = view.findViewById(R.id.tvItemKm5);
        cvItem1 = view.findViewById(R.id.cardViewItem1);
        cvItem2 = view.findViewById(R.id.cardViewItem2);
        cvItem3 = view.findViewById(R.id.cardViewItem3);
        cvItem4 = view.findViewById(R.id.cardViewItem4);
        cvItem5 = view.findViewById(R.id.cardViewItem5);
        layoutProgressBar = view.findViewById(R.id.layoutProgressBar);
        btnCart = view.findViewById(R.id.btnCart);
        tvSizeOfCart = view.findViewById(R.id.tvSizeOfCart);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, RequestsCode.REQUEST_LOCATION);
        }

        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SearchActivity.class));
            }
        });
        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), PickLocationActivity.class), RequestsCode.REQUEST_ADDRESS);
            }
        });

        cvItem1.setClickable(false);
        cvItem2.setClickable(false);
        cvItem3.setClickable(false);
        cvItem4.setClickable(false);
        cvItem5.setClickable(false);
        cvItem1.setEnabled(false);
        cvItem2.setEnabled(false);
        cvItem3.setEnabled(false);
        cvItem4.setEnabled(false);
        cvItem5.setEnabled(false);

        cvItem1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                intent.putExtra("id", branches.get(0).getId());
                intent.putExtra("name", branches.get(0).getName());
                intent.putExtra("phone", branches.get(0).getPhoneNumber());
                intent.putExtra("address", branches.get(0).getAddress());
                intent.putExtra("imgURL", branches.get(0).getImageUrl());
                intent.putExtra("openTime", branches.get(0).getOpeningTime());
                intent.putExtra("closeTime", branches.get(0).getClosingTime());
                intent.putExtra("isSell", branches.get(0).isSell());
                intent.putExtra("lat", branches.get(0).getLatLng().latitude);
                intent.putExtra("lng", branches.get(0).getLatLng().longitude);
                startActivity(intent);
            }
        });
        cvItem2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                intent.putExtra("id", branches.get(1).getId());
                intent.putExtra("name", branches.get(1).getName());
                intent.putExtra("phone", branches.get(1).getPhoneNumber());
                intent.putExtra("address", branches.get(1).getAddress());
                intent.putExtra("imgURL", branches.get(1).getImageUrl());
                intent.putExtra("openTime", branches.get(1).getOpeningTime());
                intent.putExtra("closeTime", branches.get(1).getClosingTime());
                intent.putExtra("isSell", branches.get(1).isSell());
                intent.putExtra("lat", branches.get(1).getLatLng().latitude);
                intent.putExtra("lng", branches.get(1).getLatLng().longitude);
                startActivity(intent);
            }
        });
        cvItem3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                intent.putExtra("id", branches.get(2).getId());
                intent.putExtra("name", branches.get(2).getName());
                intent.putExtra("phone", branches.get(2).getPhoneNumber());
                intent.putExtra("address", branches.get(2).getAddress());
                intent.putExtra("imgURL", branches.get(2).getImageUrl());
                intent.putExtra("openTime", branches.get(2).getOpeningTime());
                intent.putExtra("closeTime", branches.get(2).getClosingTime());
                intent.putExtra("isSell", branches.get(2).isSell());
                intent.putExtra("lat", branches.get(2).getLatLng().latitude);
                intent.putExtra("lng", branches.get(2).getLatLng().longitude);
                startActivity(intent);
            }
        });
        cvItem4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                intent.putExtra("id", branches.get(3).getId());
                intent.putExtra("name", branches.get(3).getName());
                intent.putExtra("phone", branches.get(3).getPhoneNumber());
                intent.putExtra("address", branches.get(3).getAddress());
                intent.putExtra("imgURL", branches.get(3).getImageUrl());
                intent.putExtra("openTime", branches.get(3).getOpeningTime());
                intent.putExtra("closeTime", branches.get(3).getClosingTime());
                intent.putExtra("isSell", branches.get(3).isSell());
                intent.putExtra("lat", branches.get(3).getLatLng().latitude);
                intent.putExtra("lng", branches.get(3).getLatLng().longitude);
                startActivity(intent);
            }
        });
        cvItem5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                intent.putExtra("id", branches.get(4).getId());
                intent.putExtra("name", branches.get(4).getName());
                intent.putExtra("phone", branches.get(4).getPhoneNumber());
                intent.putExtra("address", branches.get(4).getAddress());
                intent.putExtra("imgURL", branches.get(4).getImageUrl());
                intent.putExtra("openTime", branches.get(4).getOpeningTime());
                intent.putExtra("closeTime", branches.get(4).getClosingTime());
                intent.putExtra("isSell", branches.get(4).isSell());
                intent.putExtra("lat", branches.get(4).getLatLng().latitude);
                intent.putExtra("lng", branches.get(4).getLatLng().longitude);
                startActivity(intent);
            }
        });

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "")+"";

        String TOKEN_PREFIX = "Bearer ";
        JWT jwt = new JWT(token.replace(TOKEN_PREFIX,""));
        username = jwt.getSubject();
        Claim claim = jwt.getClaim("userId");
        userId = claim.asInt();

        showCurrentLocation();

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleSmall);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        layoutProgressBar.addView(progressBar, params);
        progressBar.setVisibility(View.VISIBLE);

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CartDialog(Objects.requireNonNull(getActivity()), R.style.CartDialog).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestsCode.REQUEST_ADDRESS && resultCode == Activity.RESULT_OK && data != null){
            tvAddress.setText(data.getStringExtra("ADDRESS"));
            // update location
            if (!token.equals("")) {
                double lat = data.getDoubleExtra("latitude", 0);
                double lng = data.getDoubleExtra("longitude", 0);
                String address = data.getStringExtra("ADDRESS");
                new UpdateLocationTask(getActivity(), lat, lng, address, consumerID).execute();
                // store consumer address info
                SharedPreferences.Editor editor = Objects.requireNonNull(getActivity()).getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE).edit();
                editor.putString(Shared.KEY_ADDRESS, address);
                editor.putString(Shared.KEY_LATITUDE, lat+"");
                editor.putString(Shared.KEY_LONGITUDE, lng+"");
                editor.apply();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RequestsCode.REQUEST_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            showCurrentLocation();
        }
    }

    private void showCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            // show branch
                            new GetBranch().execute();

                            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                Address address = addresses.get(0);
                                addressLine = address.getAddressLine(0);
                                tvAddress.setText(addressLine);

                                // save location to DB
                                if (!token.equals("")) {
                                    new ConsumerTask().execute();
                                }
                            } catch (IOException e) {
                                Log.i("HomeFragment", "error: " + e.getMessage());
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("HomeFragment", "error: " + e.getMessage());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressLint("StaticFieldLeak")
    class ConsumerTask extends AsyncTask<String, String, String> {
        private InputStream is;
        @Override
        protected String doInBackground(String... strings) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(RequestUrl.CONSUMER + "user/" +userId);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", token);
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.connect();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", ""+statusCode);
                if (statusCode >= 200 && statusCode < 400){
                    is = connection.getInputStream();
                } else {
                    is = connection.getErrorStream();
                }

                reader = new BufferedReader(new InputStreamReader(is));

                StringBuilder buffer = new StringBuilder();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    Log.d("ResponseConsumer: ", "> " + line);
                }

                return buffer.toString();
            } catch (SocketTimeoutException e) {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
            } catch (IOException e){
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) return;

            try {
                JSONObject consumer = new JSONObject(s);
                consumerID = consumer.getInt("id");
                consumerLocation = consumer.getString("consumerLocation");
                if (consumerLocation.equals("null")){
                    new SaveLocationTask(getActivity(), currentLocation.latitude, currentLocation.longitude, addressLine, consumerID).execute();
                } else {
                    new UpdateLocationTask(getActivity(), currentLocation.latitude, currentLocation.longitude, addressLine, consumerID).execute();
                }

                // store some consumer info
                SharedPreferences.Editor editor = Objects.requireNonNull(getActivity()).getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE).edit();
                editor.putInt(Shared.KEY_CONSUMER_ID, consumerID);
                editor.putString(Shared.KEY_USERNAME, username);
                editor.putString(Shared.KEY_FIRST_NAME, consumer.getString("firstName"));
                editor.putString(Shared.KEY_LAST_NAME, consumer.getString("lastName"));
                editor.putString(Shared.KEY_PHONE, consumer.getString("phoneNumber"));
                if (!consumerLocation.equals("null")){
                    JSONObject jsonLocation = new JSONObject(consumerLocation);
                    editor.putString(Shared.KEY_ADDRESS, jsonLocation.getString("address"));
                    editor.putString(Shared.KEY_LATITUDE, jsonLocation.getDouble("latitude")+"");
                    editor.putString(Shared.KEY_LONGITUDE, jsonLocation.getDouble("longitude")+"");
                }
                editor.apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class SaveLocationTask extends AsyncTask<String,String,String> {
        private Context context;
        private OutputStream os;
        private InputStream is;
        private double latitude, longitude;
        private String address;
        private int consumerID;
        private final String locationURL = "https://orefoo.herokuapp.com/consumer-location";

        public SaveLocationTask(Context context, double latitude, double longitude, String address, int consumerID){
            this.context = context;
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.consumerID = consumerID;
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(locationURL);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("latitude", latitude);
                jsonObject.put("longitude", longitude);
                jsonObject.put("address", address);
                JSONObject jsonConsumer = new JSONObject();
                jsonConsumer.put("id", consumerID);
                jsonObject.put("consumer", jsonConsumer);
                String data = jsonObject.toString();

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Authorization", token);
                connection.setDoOutput(true);
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setFixedLengthStreamingMode(data.getBytes().length);
                connection.connect();

                os = new BufferedOutputStream(connection.getOutputStream());
                os.write(data.getBytes());
                os.flush();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+"");
                if (statusCode >= 200 && statusCode < 400){
                    is = connection.getInputStream();
                } else {
                    is = connection.getErrorStream();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                    Log.d("ResponseSaveLocation: ", "> " + line);
                }
                return buffer.toString();

            } catch (SocketTimeoutException e) {
                Toast.makeText(context, context.getResources().getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
            } catch (IOException | JSONException e){
                e.printStackTrace();
            } finally {
                try {
                    if (os!=null) os.close();
                    if (is!=null) is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (connection != null) connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s==null) return;
        }
    }

    @SuppressLint("StaticFieldLeak")
    class UpdateLocationTask extends AsyncTask<String,String,String> {
        private Context context;
        private OutputStream os;
        private InputStream is;
        private double latitude, longitude;
        private String address;
        private int consumerID;
        private final String locationURL = "https://orefoo.herokuapp.com/consumer-location?consumer-id=";

        public UpdateLocationTask(Context context, double latitude, double longitude, String address, int consumerID){
            this.context = context;
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.consumerID = consumerID;
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(locationURL+consumerID);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("latitude", latitude);
                jsonObject.put("longitude", longitude);
                jsonObject.put("address", address);
                String data = jsonObject.toString();

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PATCH");
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Authorization", token);
                connection.setDoOutput(true);
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setFixedLengthStreamingMode(data.getBytes().length);
                connection.connect();

                os = new BufferedOutputStream(connection.getOutputStream());
                os.write(data.getBytes());
                os.flush();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+"");
                if (statusCode >= 200 && statusCode < 400){
                    is = connection.getInputStream();
                } else {
                    is = connection.getErrorStream();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                    Log.d("ResponseUpdateLocation", "> " + line);
                }
                return buffer.toString();

            } catch (SocketTimeoutException e) {
                Toast.makeText(context, context.getResources().getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
            } catch (IOException | JSONException e){
                e.printStackTrace();
            } finally {
                try {
                    if (os!=null) os.close();
                    if (is!=null) is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (connection != null) connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s==null) return;
        }
    }

    @SuppressLint("StaticFieldLeak")
    class GetBranch extends AsyncTask<String,String,String> {
        private InputStream is;
        private final String branchURL = "https://orefoo.herokuapp.com/branch?page=1";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(branchURL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", token);
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.connect();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+"");
                if (statusCode >= 200 && statusCode < 400){
                    is = connection.getInputStream();
                } else {
                    is = connection.getErrorStream();
                }

                reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                    Log.d("ResponseGetBranch: ", "> " + line);
                }

                return buffer.toString();
            } catch (SocketTimeoutException e) {
            } catch (IOException e){
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressBar.setVisibility(View.INVISIBLE);

            if (s == null) return;

            cvItem1.setEnabled(true);
            cvItem2.setEnabled(true);
            cvItem3.setEnabled(true);
            cvItem4.setEnabled(true);
            cvItem5.setEnabled(true);

            try {
                branches.clear();
                // add branches
                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < Math.min(jsonArray.length(), 5); i++){
                    final JSONObject jsonObject = jsonArray.getJSONObject(i);
                    branches.add(new Branch(jsonObject.getInt("id"), jsonObject.getString("name"), jsonObject.getString("phoneNumber"),
                            jsonObject.getString("imageUrl"), jsonObject.getString("openingTime"), jsonObject.getString("closingTime"),
                            jsonObject.getString("address"), new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")),
                            jsonObject.getBoolean("isSell"), jsonObject.getInt("merchant"), jsonObject.getString("branchStatus")));
                }

                // sort branch following distance
                if (currentLocation != null) Collections.sort(branches, new SortPlaces(currentLocation));
                else {
                    Toast.makeText(getActivity(), "Cannot get current location, try again", Toast.LENGTH_LONG).show();
                    return;
                }

                // show branch
                for (int index = 0; index<(Math.min(jsonArray.length(), 5)); index++){
                    switch (index){
                        case 0:
                            updateUIBranchSuggest(getActivity(), ivItem1, tvItemName1, tvKm1,
                                    branches.get(0).getImageUrl(), branches.get(0).getName(), GetDistanceInKmString(currentLocation, branches.get(0).getLatLng()));
                            break;

                        case 1:
                            updateUIBranchSuggest(getActivity(), ivItem2, tvItemName2, tvKm2,
                                    branches.get(1).getImageUrl(), branches.get(1).getName(), GetDistanceInKmString(currentLocation, branches.get(1).getLatLng()));
                            break;

                        case 2:
                            updateUIBranchSuggest(getActivity(), ivItem3, tvItemName3, tvKm3,
                                    branches.get(2).getImageUrl(), branches.get(2).getName(), GetDistanceInKmString(currentLocation, branches.get(2).getLatLng()));
                            break;

                        case 3:
                            updateUIBranchSuggest(getActivity(), ivItem4, tvItemName4, tvKm4,
                                    branches.get(3).getImageUrl(), branches.get(3).getName(), GetDistanceInKmString(currentLocation, branches.get(3).getLatLng()));
                            break;

                        case 4:
                            updateUIBranchSuggest(getActivity(), ivItem5, tvItemName5, tvKm5,
                                    branches.get(4).getImageUrl(), branches.get(4).getName(), GetDistanceInKmString(currentLocation, branches.get(4).getLatLng()));
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUIBranchSuggest(Context context, ImageView branchImage, TextView branchName, TextView branchDistance, String imageUrl, String name, String distance){
        // Branch image
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.image_not_found);
        Glide.with(context).load(imageUrl).apply(options).into(branchImage);

        // Branch name
        branchName.setText(name);

        // Distance from Branch to Consumer location
        branchDistance.setText(distance);
    }

    public double rad(double x){
        return x * Math.PI / 180;
    }
    public double GetDistance(LatLng p1, LatLng p2){
        double R = 6378137; // Earth’s mean radius in meter
        double dLat = rad(p2.latitude - p1.latitude);
        double dLng = rad(p2.longitude - p1.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(rad(p1.latitude)) * Math.cos(rad(p2.latitude)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    @SuppressLint("DefaultLocale")
    public String GetDistanceInKmString(LatLng p1, LatLng p2){
        return String.format("%.1f", GetDistance(p1, p2)/1000) + "Km";
    }

    // Get Cart Item Size
    @SuppressLint("StaticFieldLeak")
    class CartItemTask extends AsyncTask<String,String,String> {
        private InputStream is;
        private int cartID;
        private final String cartItemURL = "https://orefoo.herokuapp.com/cart-item?cart-id=";
        private int resultCode;

        public void getAllItem(int cartID){
            this.cartID = cartID;
            if (cartID != -1) execute();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(cartItemURL + cartID);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", token);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.connect();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+"");

                if (statusCode >= 200 && statusCode < 400){
                    is = connection.getInputStream();
                    resultCode = ResultsCode.SUCCESS;
                } else {
                    is = connection.getErrorStream();
                    if (statusCode == 406) {
                        resultCode = ResultsCode.DIFFERENCE_BRANCH;
                    } else resultCode = ResultsCode.FAILED;
                }

                reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                    Log.d("ResponseCartItem: ", "> " + line);
                }

                return buffer.toString();
            } catch (SocketTimeoutException e) {
                resultCode = ResultsCode.SOCKET_TIMEOUT;
            } catch (IOException e){
                resultCode = ResultsCode.IO_EXCEPTION;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressBar.setVisibility(View.INVISIBLE);

            String sizeCart = "0";

            if (s == null) return;

            switch (resultCode) {
                case ResultsCode.SUCCESS:
                    Log.i("result", "get cart item success");
                    try {
                        JSONArray jsonArray = new JSONArray(s);
                        sizeCart = jsonArray.length()+"";
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case ResultsCode.DIFFERENCE_BRANCH:
                    break;
                case ResultsCode.FAILED:
                    Log.i("result", "get failed");
                    break;
                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.socket_timeout), Toast.LENGTH_SHORT).show();
                    break;
                case ResultsCode.IO_EXCEPTION:
                    Toast.makeText(getContext(), "IO Exception", Toast.LENGTH_SHORT).show();
                    break;
            }
            tvSizeOfCart.setText(sizeCart);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // show size cart to Cart Button
        new CartItemTask().getAllItem(cartId);
    }
}