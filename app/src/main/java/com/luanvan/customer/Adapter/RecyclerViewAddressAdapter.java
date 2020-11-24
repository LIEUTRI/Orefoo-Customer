package com.luanvan.customer.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.luanvan.customer.R;
import com.luanvan.customer.components.Shared;
import com.luanvan.customer.components.UserLocation;

import java.util.List;

public class RecyclerViewAddressAdapter extends RecyclerView.Adapter<RecyclerViewAddressAdapter.ViewHolder> {
    private List<UserLocation> list;
    private Activity activity;

    public RecyclerViewAddressAdapter(Activity activity, List<UserLocation> list){
        this.activity = activity;
        this.list = list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.location_item, parent, false);
        return new RecyclerViewAddressAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final UserLocation userLocation = list.get(position);
        holder.tvLocationName.setText(userLocation.getLocationName());
        holder.tvLocationAddress.setText(userLocation.getLocationAddress());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent data = new Intent();
                data.putExtra(Shared.KEY_ADDRESS, userLocation.getLocationAddress());
                data.putExtra(Shared.KEY_LATITUDE, userLocation.getLatitude());
                data.putExtra(Shared.KEY_LONGITUDE, userLocation.getLongitude());
                activity.setResult(Activity.RESULT_OK, data);
                activity.finish();
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvLocationName;
        public TextView tvLocationAddress;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocationName = itemView.findViewById(R.id.tvLocationName);
            tvLocationAddress = itemView.findViewById(R.id.tvLocationAddress);
        }
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
}
