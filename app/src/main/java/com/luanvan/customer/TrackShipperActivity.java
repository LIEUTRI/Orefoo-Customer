package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.luanvan.customer.Adapter.RecyclerViewVictualAdapter;
import com.luanvan.customer.Fragments.MapsFragment;
import com.luanvan.customer.components.Victual;

import java.util.ArrayList;

public class TrackShipperActivity extends AppCompatActivity {
    private ArrayList<Victual> victuals = new ArrayList<>();
    private double victualsPrice, shippingFee, totalPay;

    private RecyclerView recyclerView;
    private MaterialToolbar toolbar;
    private TextView tvVictualsPrice, tvShippingFee, tvTotalPay;
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_shipper);

        tvVictualsPrice = findViewById(R.id.tvTotal);
        tvShippingFee = findViewById(R.id.tvShipFee);
        tvTotalPay = findViewById(R.id.tvTotalFinal);

        showMaps();

        victuals = (ArrayList<Victual>) getIntent().getSerializableExtra("victuals");
        victualsPrice = getIntent().getDoubleExtra("victualsPrice", 0);
        shippingFee = getIntent().getDoubleExtra("shippingFee", 0);
        totalPay = getIntent().getDoubleExtra("totalPay", 0);
        recyclerView = findViewById(R.id.recyclerView);
        toolbar = findViewById(R.id.toolbar);

        tvVictualsPrice.setText(String.format("%,.0f", victualsPrice) + "đ");
        tvShippingFee.setText(String.format("%,.0f", shippingFee) + "đ");
        tvTotalPay.setText(String.format("%,.0f", totalPay) + "đ");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(new RecyclerViewVictualAdapter(this, victuals));
    }

    private void showMaps(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        MapsFragment mapsFragment = new MapsFragment();
        fragmentTransaction.add(R.id.fragment_maps_container, mapsFragment);
        fragmentTransaction.commit();
    }
}