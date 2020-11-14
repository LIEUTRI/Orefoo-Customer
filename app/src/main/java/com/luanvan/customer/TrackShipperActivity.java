package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.luanvan.customer.Adapter.RecyclerViewVictualAdapter;
import com.luanvan.customer.Fragments.MapsFragment;
import com.luanvan.customer.components.Victual;

import java.util.ArrayList;

public class TrackShipperActivity extends AppCompatActivity {
    private ArrayList<Victual> victuals = new ArrayList<>();
    private RecyclerView recyclerView;
    private MaterialToolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_shipper);

        showMaps();

        victuals = (ArrayList<Victual>) getIntent().getSerializableExtra("victuals");

        recyclerView = findViewById(R.id.recyclerView);
        toolbar = findViewById(R.id.toolbar);

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