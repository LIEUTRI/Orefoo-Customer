package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;
import com.luanvan.customer.Fragments.CommentFragment;
import com.luanvan.customer.Fragments.MenuFragment;
import com.luanvan.customer.Fragments.RestaurantInfoFragment;
import com.luanvan.customer.components.AddItemDialog;
import com.luanvan.customer.components.CalculateDistanceTime;
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
    private TextView tvDistance, tvTime;
    private AppBarLayout appBarLayout;
    private AlertDialog dialogDistance;

    private String name;
    private String phone;
    private String imgURL;
    private String openTime, closeTime;
    private String address;
    private Boolean isSell;
    private int id;
    private double latitude, longitude;
    public static boolean tooFar = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        tvMenu = findViewById(R.id.tvMenu);
        tvComment = findViewById(R.id.tvComment);
        tvInfo = findViewById(R.id.tvInfo);
        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvDistance = findViewById(R.id.tvDistance);
        tvTime = findViewById(R.id.tvTime);
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

        // consumer location
        SharedPreferences sharedPreferences = getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE);
        LatLng consumerLatLng = new LatLng(Double.parseDouble(sharedPreferences.getString(Shared.KEY_LATITUDE, "0")),
                Double.parseDouble(sharedPreferences.getString(Shared.KEY_LONGITUDE, "0")));

        Log.i("RestaurantActivity", "location: "+consumerLatLng.latitude+"| "+latitude);

        CalculateDistanceTime distance_task = new CalculateDistanceTime(this);
        distance_task.getDirectionsUrl(consumerLatLng, new LatLng(latitude,longitude));
        distance_task.setLoadListener(new CalculateDistanceTime.taskCompleteListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void taskCompleted(String[] distance) {
                tvDistance.setText("unknown");
                tvTime.setText("unknown");
                if (distance.length > 0){
                    tvDistance.setText(distance[0]);
                    tvTime.setText(distance[1]);

                    double km = Double.parseDouble(distance[0].substring(0,distance[0].indexOf(" ")));
                    if (km <= 10.0){
                        SharedPreferences.Editor editor = getSharedPreferences(Shared.BRANCH, Context.MODE_PRIVATE).edit();
                        editor.putString(Shared.KEY_BRANCH_DISTANCE, km+"");
                        editor.apply();
                    } else {
                        tooFar = true;
                        showDialogDistance(RestaurantActivity.this);
                    }
                }
            }
        });

        tvName.setText(name);
        tvAddress.setText(address);
    }

    private void showDialogDistance(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.too_far));
        builder.setMessage(context.getResources().getString(R.string.too_far_message));
        builder.setCancelable(false);
        builder.setPositiveButton(
                context.getResources().getString(R.string.change_branch),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }
        );
        builder.setNegativeButton(
                context.getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }
        );

        dialogDistance = builder.create();
        dialogDistance.show();
    }
}