package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.luanvan.customer.Fragments.CommentFragment;
import com.luanvan.customer.Fragments.MenuFragment;
import com.luanvan.customer.Fragments.RestaurantInfoFragment;
import com.luanvan.customer.components.Shared;
import com.luanvan.customer.components.UserData;

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
    private AppBarLayout appBarLayout;

    private String name;
    private String phone;
    private String imgURL;
    private String openTime, closeTime;
    private String address;
    private Boolean isSell;
    private int id;
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
        appBarLayout = findViewById(R.id.layoutAppBar);

        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        imgURL = getIntent().getStringExtra("imgURL");
        openTime = getIntent().getStringExtra("openTime");
        closeTime = getIntent().getStringExtra("closeTime");
        address = getIntent().getStringExtra("address");
        isSell = getIntent().getBooleanExtra("isSell", true);
        id = getIntent().getIntExtra("id", -1);
        latitude = getIntent().getDoubleExtra("lat", 0);
        longitude = getIntent().getDoubleExtra("lng", 0);

        SharedPreferences.Editor editor = getSharedPreferences(Shared.BRANCH, Context.MODE_PRIVATE).edit();
        editor.putString(Shared.KEY_BRANCH_NAME, name);
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
    }
}