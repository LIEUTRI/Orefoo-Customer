package com.luanvan.customer.Fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.luanvan.customer.Adapter.ViewPagerOrdersAdapter;
import com.luanvan.customer.R;

import java.util.ArrayList;

public class OrderFragment extends Fragment {

    private ViewPager2 viewPager;
    private ViewPagerOrdersAdapter viewPagerOrdersAdapter;
    private ArrayList<Fragment> listFragment = new ArrayList<>();
    private ViewPager2.OnPageChangeCallback onPageChangeCallback;
    private TextView tvOngoing, tvHistory;

    public OrderFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = view.findViewById(R.id.viewpagerOrders);
        tvOngoing = view.findViewById(R.id.tvOngoing);
        tvHistory = view.findViewById(R.id.tvHistory);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // ViewPager2 /////////////////////////////////////////
        listFragment.add(new OngoingFragment());
        listFragment.add(new HistoryFragment());

        viewPagerOrdersAdapter = new ViewPagerOrdersAdapter(getActivity().getSupportFragmentManager(), getActivity().getLifecycle());
        viewPager.setAdapter(viewPagerOrdersAdapter);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setPageTransformer(new MarginPageTransformer(1000));

        tvOngoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0, true);
            }
        });
        tvHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1,true);
            }
        });

        onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position){
                    case 0: updateIconUI(tvOngoing); break;
                    case 1: updateIconUI(tvHistory); break;
                }
            }
        };
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);
        ///////////////////////////////////////////////////////
    }

    private void updateIconUI(TextView tv){
        switch (tv.getId()){
            case R.id.tvOngoing:
                tvOngoing.setTypeface(null, Typeface.BOLD);
                tvHistory.setTypeface(null, Typeface.NORMAL);
                tvOngoing.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                tvHistory.setTextColor(ContextCompat.getColor(getActivity(),R.color.light_gray));
                tvOngoing.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_bottom_border_btn));
                tvHistory.setBackgroundColor(Color.WHITE);
                break;
            case R.id.tvHistory:
                tvOngoing.setTypeface(null, Typeface.NORMAL);
                tvHistory.setTypeface(null, Typeface.BOLD);
                tvOngoing.setTextColor(ContextCompat.getColor(getActivity(),R.color.light_gray));
                tvHistory.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                tvOngoing.setBackgroundColor(Color.WHITE);
                tvHistory.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_bottom_border_btn));
                break;
        }
    }
}