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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.luanvan.customer.components.CartDialog;
import com.luanvan.customer.components.RequestsCode;
import com.luanvan.customer.components.ResultsCode;
import com.luanvan.customer.components.Shared;

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
    private String consumerID = "";
    private String consumerLocation = "";
    private String token = "";
    private int cartId;

    public static LatLng currentLocation;
    private String addressLine = "";

    private String name1, name2, name3, name4, name5;
    private String address1, address2, address3, address4, address5;
    private String id1, id2, id3, id4, id5;
    private String imgURL1, imgURL2, imgURL3, imgURL4, imgURL5;
    private String phone1, phone2, phone3, phone4, phone5;
    private Boolean isSell1, isSell2, isSell3, isSell4, isSell5;
    private String openTime1, openTime2, openTime3, openTime4, openTime5;
    private String closeTime1, closeTime2, closeTime3, closeTime4, closeTime5;
    private LatLng latLng1, latLng2, latLng3, latLng4, latLng5;

    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;

    private RequestOptions options;

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
                intent.putExtra("name", name1);
                intent.putExtra("address", address1);
                intent.putExtra("id", id1);
                intent.putExtra("openTime", openTime1);
                intent.putExtra("closeTime", closeTime1);
                intent.putExtra("isSell", isSell1);
                intent.putExtra("phone", phone1);
                intent.putExtra("imgURL", imgURL1);
                intent.putExtra("lat", latLng1.latitude);
                intent.putExtra("lng", latLng1.longitude);
                startActivity(intent);
            }
        });
        cvItem2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                intent.putExtra("name", name2);
                intent.putExtra("address", address2);
                intent.putExtra("id", id2);
                intent.putExtra("openTime", openTime2);
                intent.putExtra("closeTime", closeTime2);
                intent.putExtra("isSell", isSell2);
                intent.putExtra("phone", phone2);
                intent.putExtra("imgURL", imgURL2);
                intent.putExtra("lat", latLng2.latitude);
                intent.putExtra("lng", latLng2.longitude);
                startActivity(intent);
            }
        });
        cvItem3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                intent.putExtra("name", name3);
                intent.putExtra("address", address3);
                intent.putExtra("id", id3);
                intent.putExtra("openTime", openTime3);
                intent.putExtra("closeTime", closeTime3);
                intent.putExtra("isSell", isSell3);
                intent.putExtra("phone", phone3);
                intent.putExtra("imgURL", imgURL3);
                intent.putExtra("lat", latLng3.latitude);
                intent.putExtra("lng", latLng3.longitude);
                startActivity(intent);
            }
        });
        cvItem4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                intent.putExtra("name", name4);
                intent.putExtra("address", address4);
                intent.putExtra("id", id4);
                intent.putExtra("openTime", openTime4);
                intent.putExtra("closeTime", closeTime4);
                intent.putExtra("isSell", isSell4);
                intent.putExtra("phone", phone4);
                intent.putExtra("imgURL", imgURL4);
                intent.putExtra("lat", latLng4.latitude);
                intent.putExtra("lng", latLng4.longitude);
                startActivity(intent);
            }
        });
        cvItem5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                intent.putExtra("name", name5);
                intent.putExtra("address", address5);
                intent.putExtra("id", id5);
                intent.putExtra("openTime", openTime5);
                intent.putExtra("closeTime", closeTime5);
                intent.putExtra("isSell", isSell5);
                intent.putExtra("phone", phone5);
                intent.putExtra("imgURL", imgURL5);
                intent.putExtra("lat", latLng5.latitude);
                intent.putExtra("lng", latLng5.longitude);
                startActivity(intent);
            }
        });

        // Glide - load image from url
        options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.image_not_found);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "")+"";

        showCurrentLocation();

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleSmall);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        layoutProgressBar.addView(progressBar, params);
        progressBar.setVisibility(View.VISIBLE);

        final SwipeRefreshLayout pullToRefresh = getActivity().findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showCurrentLocation();
                pullToRefresh.setRefreshing(false);
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CartDialog(Objects.requireNonNull(getActivity()), R.style.CartDialog).show();
            }
        });

        new CartItemTask().getAllItem(cartId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestsCode.REQUEST_ADDRESS && resultCode == Activity.RESULT_OK && data != null){
            tvAddress.setText(data.getStringExtra("ADDRESS"));
            // update location
            if (!token.equals(""))
                new UpdateLocationTask(getActivity(), data.getDoubleExtra("latitude", 0), data.getDoubleExtra("longitude", 0), data.getStringExtra("ADDRESS"), consumerID).execute();
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
                            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                Address address = addresses.get(0);
                                addressLine = address.getAddressLine(0);
                                tvAddress.setText(addressLine);

                                // save location to DB
                                if (!token.equals("")){
                                    new GetConsumerID().execute();
                                }

                                // show branch
                                new GetBranch().execute();
                            } catch (IOException e) {
                                Log.i("HomeFragment", "error: "+e.getMessage());
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("HomeFragment", "error: "+e.getMessage());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    class GetConsumerID extends AsyncTask<String, String, String> {
        private InputStream is;
        private String user;
        private final String consumerURL = "https://orefoo.herokuapp.com/user/consumer";
        @Override
        protected String doInBackground(String... strings) {

            String TOKEN_PREFIX = "Bearer ";
            JWT jwt = new JWT(token.replace(TOKEN_PREFIX,""));
            user = jwt.getSubject();

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(consumerURL+"?username="+user);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", token);
                connection.connect();

                is = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(is));

                StringBuilder buffer = new StringBuilder();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    Log.d("Response: ", "> " + line);
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
                JSONObject user = new JSONObject(s);
                JSONObject consumer = user.getJSONObject("consumer");
                consumerID = consumer.getString("id");
                consumerLocation = consumer.getString("consumerLocation");
                if (consumerLocation.equals("null")){
                    new SaveLocationTask(getActivity(), currentLocation.latitude, currentLocation.longitude, addressLine, consumerID)
                            .execute();
                } else {
                    new UpdateLocationTask(getActivity(), currentLocation.latitude, currentLocation.longitude, addressLine, consumerID)
                            .execute();
                }

                // store some consumer info
                SharedPreferences.Editor editor = Objects.requireNonNull(getActivity()).getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE).edit();
                editor.putInt(Shared.KEY_CONSUMER_ID, consumer.getInt("id"));
                editor.putString(Shared.KEY_FIRST_NAME, consumer.getString("firstName"));
                editor.putString(Shared.KEY_LAST_NAME, consumer.getString("lastName"));
                editor.putString(Shared.KEY_PHONE, consumer.getString("phoneNumber"));
                JSONObject jsonLocation = new JSONObject(consumerLocation);
                editor.putString(Shared.KEY_ADDRESS, jsonLocation.getString("address"));
                editor.putString(Shared.KEY_LATITUDE, jsonLocation.getDouble("latitude")+"");
                editor.putString(Shared.KEY_LONGITUDE, jsonLocation.getDouble("longitude")+"");
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
        private String consumerID;
        private final String locationURL = "https://orefoo.herokuapp.com/consumer-location";

        public SaveLocationTask(Context context, double latitude, double longitude, String address, String consumerID){
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
                    Log.d("Response: ", "> " + line);
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
        private String consumerID;
        private final String locationURL = "https://orefoo.herokuapp.com/consumer-location?consumer-id=";

        public UpdateLocationTask(Context context, double latitude, double longitude, String address, String consumerID){
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
                    Log.d("Response: ", "> " + line);
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
                    Log.d("Response: ", "> " + line);
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
                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < Math.min(jsonArray.length(), 5); i++){
                    final JSONObject jsonObject = jsonArray.getJSONObject(i);

                    switch (i){
                        case 0:
                            Glide.with(getActivity()).load(jsonObject.getString("imageUrl")).apply(options).into(ivItem1);
                            try {
                                id1 = jsonObject.getString("id");
                                name1 = jsonObject.getString("name");
                                address1 = jsonObject.getString("address");
                                imgURL1 = jsonObject.getString("imageUrl");
                                phone1 = jsonObject.getString("phoneNumber");
                                isSell1 = jsonObject.getBoolean("isSell");
                                openTime1 = jsonObject.getString("openingTime");
                                closeTime1 = jsonObject.getString("closingTime");
                                tvItemName1.setText(name1);
                                latLng1 = new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude"));
                                tvKm1.setText(GetDistanceInKmString(currentLocation, latLng1));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;

                        case 1:
                            Glide.with(getActivity()).load(jsonObject.getString("imageUrl")).apply(options).into(ivItem2);
                            try {
                                id2 = jsonObject.getString("id");
                                name2 = jsonObject.getString("name");
                                address2 = jsonObject.getString("address");
                                imgURL2 = jsonObject.getString("imageUrl");
                                phone2 = jsonObject.getString("phoneNumber");
                                isSell2 = jsonObject.getBoolean("isSell");
                                openTime2 = jsonObject.getString("openingTime");
                                closeTime2 = jsonObject.getString("closingTime");
                                tvItemName2.setText(jsonObject.getString("name"));
                                latLng2 = new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude"));
                                tvKm2.setText(GetDistanceInKmString(currentLocation, latLng2));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;

                        case 2:
                            Glide.with(getActivity()).load(jsonObject.getString("imageUrl")).apply(options).into(ivItem3);
                            try {
                                id3 = jsonObject.getString("id");
                                name3 = jsonObject.getString("name");
                                address3 = jsonObject.getString("address");
                                imgURL3 = jsonObject.getString("imageUrl");
                                phone3 = jsonObject.getString("phoneNumber");
                                isSell3 = jsonObject.getBoolean("isSell");
                                openTime3 = jsonObject.getString("openingTime");
                                closeTime3 = jsonObject.getString("closingTime");
                                tvItemName3.setText(jsonObject.getString("name"));
                                latLng3 = new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude"));
                                tvKm3.setText(GetDistanceInKmString(currentLocation, latLng3));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;

                        case 3:
                            Glide.with(getActivity()).load(jsonObject.getString("imageUrl")).apply(options).into(ivItem4);
                            try {
                                id4 = jsonObject.getString("id");
                                name4 = jsonObject.getString("name");
                                address4 = jsonObject.getString("address");
                                imgURL4 = jsonObject.getString("imageUrl");
                                phone4 = jsonObject.getString("phoneNumber");
                                isSell4 = jsonObject.getBoolean("isSell");
                                openTime4 = jsonObject.getString("openingTime");
                                closeTime4 = jsonObject.getString("closingTime");
                                tvItemName4.setText(jsonObject.getString("name"));
                                latLng4 = new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude"));
                                tvKm4.setText(GetDistanceInKmString(currentLocation, latLng4));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;

                        case 4:
                            Glide.with(getActivity()).load(jsonObject.getString("imageUrl")).apply(options).into(ivItem5);
                            try {
                                id5 = jsonObject.getString("id");
                                name5 = jsonObject.getString("name");
                                address5 = jsonObject.getString("address");
                                imgURL5 = jsonObject.getString("imageUrl");
                                phone5 = jsonObject.getString("phoneNumber");
                                isSell5 = jsonObject.getBoolean("isSell");
                                openTime5 = jsonObject.getString("openingTime");
                                closeTime5 = jsonObject.getString("closingTime");
                                tvItemName5.setText(jsonObject.getString("name"));
                                latLng5 = new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude"));
                                tvKm5.setText(GetDistanceInKmString(currentLocation, latLng5));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
            execute();
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
        new CartItemTask().getAllItem(cartId);
    }
}