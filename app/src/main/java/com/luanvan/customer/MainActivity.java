package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.luanvan.customer.Adapter.ViewPagerFragmentAdapter;
import com.luanvan.customer.Fragments.HomeFragment;
import com.luanvan.customer.Fragments.MeFragment;
import com.luanvan.customer.Fragments.OrderFragment;
import com.luanvan.customer.components.ConnectionStateMonitor;
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
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ViewPagerFragmentAdapter viewPagerFragmentAdapter;
    private ViewPager2.OnPageChangeCallback onPageChangeCallback;
    private ArrayList<Fragment> listFragment = new ArrayList<>();
    private TextView tvHome,tvOrder,tvMe;
    private boolean doubleBackToExitPressedOnce = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewpager);
        tvHome = findViewById(R.id.tvHome);
        tvOrder = findViewById(R.id.tvOrder);
        tvMe = findViewById(R.id.tvMe);

        // ViewPager2 /////////////////////////////////////////////////////////////////////////////////////////
        listFragment.add(new HomeFragment());
        listFragment.add(new OrderFragment());
        listFragment.add(new MeFragment());

        viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setAdapter(viewPagerFragmentAdapter);
        viewPager.setPageTransformer(new MarginPageTransformer(500));
        viewPager.setUserInputEnabled(false); //disable swipe
        //////////////////////////////////////////////////////////////////////////////////////////////////////

        onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position){
                    case 0: updateIconUI(tvHome);  break;
                    case 1: updateIconUI(tvOrder); break;
                    case 2: updateIconUI(tvMe);    break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        };
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);

        tvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0,true);
            }
        });

        tvOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1,true);
            }
        });
        tvMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2,true);
            }
        });

        if (!isNetworkConnecting()){
            Toast.makeText(this, getResources().getString(R.string.check_internet_connection), Toast.LENGTH_LONG).show();
        }

        ConnectionStateMonitor monitor = new ConnectionStateMonitor();
        monitor.enable(this);
    }

    private void setTextViewDrawableColor(TextView textView, int color){
        for (Drawable drawable: textView.getCompoundDrawables()){
            if (drawable != null){
                drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            }
        }
    }
    private void updateIconUI(TextView textView){
        switch (textView.getId()){
            case R.id.tvHome:
                // update icon color
                setTextViewDrawableColor(tvHome, getResources().getColor(R.color.colorPrimary));
                setTextViewDrawableColor(tvOrder, getResources().getColor(R.color.defaultIconColor));
                setTextViewDrawableColor(tvMe, getResources().getColor(R.color.defaultIconColor));
                // update text color
                tvHome.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvOrder.setTextColor(getResources().getColor(R.color.defaultIconColor));
                tvMe.setTextColor(getResources().getColor(R.color.defaultIconColor));
                break;
            case R.id.tvOrder:
                // update icon color
                setTextViewDrawableColor(tvOrder, getResources().getColor(R.color.colorPrimary));
                setTextViewDrawableColor(tvHome, getResources().getColor(R.color.defaultIconColor));
                setTextViewDrawableColor(tvMe, getResources().getColor(R.color.defaultIconColor));
                // update text color
                tvOrder.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvHome.setTextColor(getResources().getColor(R.color.defaultIconColor));
                tvMe.setTextColor(getResources().getColor(R.color.defaultIconColor));
                break;
            case R.id.tvMe:
                // update icon color
                setTextViewDrawableColor(tvMe, getResources().getColor(R.color.colorPrimary));
                setTextViewDrawableColor(tvHome, getResources().getColor(R.color.defaultIconColor));
                setTextViewDrawableColor(tvOrder, getResources().getColor(R.color.defaultIconColor));
                // update text color
                tvMe.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvHome.setTextColor(getResources().getColor(R.color.defaultIconColor));
                tvOrder.setTextColor(getResources().getColor(R.color.defaultIconColor));
                break;
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
        viewPager.setCurrentItem(0,true);
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getResources().getString(R.string.exit_confirm), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}