package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.luanvan.customer.Adapter.ViewPagerFragmentAdapter;
import com.luanvan.customer.Fragments.HomeFragment;
import com.luanvan.customer.Fragments.MeFragment;
import com.luanvan.customer.Fragments.OrderFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ViewPager2 viewPager;
    ViewPagerFragmentAdapter viewPagerFragmentAdapter;
    ArrayList<Fragment> listFragment = new ArrayList<>();
    TextView tvHome,tvOrder,tvMe;
    ViewPager2.OnPageChangeCallback onPageChangeCallback;
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
                updateIconUI(tvHome);
            }
        });

        tvOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1,true);
                updateIconUI(tvOrder);
            }
        });
        tvMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2,true);
                updateIconUI(tvMe);
            }
        });

//        startActivity(new Intent(this, TrackShipperActivity.class));
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
}