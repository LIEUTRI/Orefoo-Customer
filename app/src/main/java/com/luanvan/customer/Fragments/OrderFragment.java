package com.luanvan.customer.Fragments;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ViewPager2 viewPager;
    private ViewPagerOrdersAdapter viewPagerOrdersAdapter;
    private ArrayList<Fragment> listFragment = new ArrayList<>();
    private ViewPager2.OnPageChangeCallback onPageChangeCallback;
    private TextView tvOngoing, tvHistory;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public OrderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderFragment newInstance(String param1, String param2) {
        OrderFragment fragment = new OrderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
        viewPager.setUserInputEnabled(false); //disable swipe

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
                break;
            case R.id.tvHistory:
                tvOngoing.setTypeface(null, Typeface.NORMAL);
                tvHistory.setTypeface(null, Typeface.BOLD);
                tvOngoing.setTextColor(ContextCompat.getColor(getActivity(),R.color.light_gray));
                tvHistory.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                break;
        }
    }
}