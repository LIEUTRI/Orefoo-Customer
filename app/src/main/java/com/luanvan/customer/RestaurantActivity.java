package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.luanvan.customer.Fragments.CommentFragment;
import com.luanvan.customer.Fragments.MenuFragment;
import com.luanvan.customer.Fragments.RestaurantInfoFragment;
import com.luanvan.customer.components.Shared;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class RestaurantActivity extends AppCompatActivity {

    private TextView tvMenu, tvComment, tvInfo;
    private TextView tvName, tvAddress;
    private String name;
    private String phone;
    private String imgURL;
    private String openTime, closeTime;
    private String address;
    private Boolean isSell;
    private String id;
    private double latitude, longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        tvMenu = findViewById(R.id.tvMenu);
        tvComment = findViewById(R.id.tvComment);
        tvInfo = findViewById(R.id.tvInfo);
        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);

        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        imgURL = getIntent().getStringExtra("imgURL");
        openTime = getIntent().getStringExtra("openTime");
        closeTime = getIntent().getStringExtra("closeTime");
        address = getIntent().getStringExtra("address");
        isSell = getIntent().getBooleanExtra("isSell", true);
        id = getIntent().getStringExtra("id");
        latitude = getIntent().getDoubleExtra("lat", 0);
        longitude = getIntent().getDoubleExtra("lng", 0);

        SharedPreferences.Editor editor = getSharedPreferences(Shared.BRANCH, Context.MODE_PRIVATE).edit();
        editor.putString(Shared.KEY_LATITUDE, latitude+"");
        editor.putString(Shared.KEY_LONGITUDE, longitude+"");
        editor.apply();

        // default fragment
        Fragment fragment = new MenuFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_restaurant_container, fragment);
        transaction.commit();

        tvMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new MenuFragment();
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_restaurant_container, fragment);
                transaction.commit();
            }
        });
        tvComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new CommentFragment();
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_restaurant_container, fragment);
                transaction.commit();
            }
        });
        tvInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new RestaurantInfoFragment();
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_restaurant_container, fragment);
                transaction.commit();
            }
        });

        tvName.setText(name);
        tvAddress.setText(address);

        SharedPreferences sharedPreferences = getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(Shared.KEY_BEARER, "");

        sharedPreferences = getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE);

        new GetCartTask().execute(token, sharedPreferences.getInt(Shared.KEY_CONSUMER_ID, -1)+"");
    }

    class GetCartTask extends AsyncTask<String,String,String> {
        private InputStream is;
        private final String victualsURL = "https://orefoo.herokuapp.com/cart?consumer-id=";
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(victualsURL + strings[1]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", strings[0]);
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
                Log.i("MenuFragment", e.getMessage());
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
            SharedPreferences.Editor editor = getSharedPreferences(Shared.CART, Context.MODE_PRIVATE).edit();
            try {
                JSONObject jsonObject = new JSONObject(s);
                editor.putInt(Shared.KEY_CART_ID, jsonObject.getInt("id"));
                editor.apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}