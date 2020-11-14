package com.luanvan.customer.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.luanvan.customer.R;
import com.luanvan.customer.RestaurantActivity;
import com.luanvan.customer.components.Branch;
import com.luanvan.customer.components.Shared;

import java.util.List;

public class RecyclerViewBranchAdapter extends RecyclerView.Adapter<RecyclerViewBranchAdapter.ViewHolder> {
    private List<Branch> list;
    private Activity activity;

    public RecyclerViewBranchAdapter(Activity activity, List<Branch> list){
        this.activity = activity;
        this.list = list;
    }
    @NonNull
    @Override
    public RecyclerViewBranchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.branch_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewBranchAdapter.ViewHolder holder, final int position) {
        final Branch branch = list.get(position);

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.image_not_found);
        Glide.with(activity).load(branch.getImageUrl()).apply(options).into(holder.ivBranch);

        holder.tvBranchName.setText(branch.getName());

        holder.tvBranchAddress.setText(branch.getAddress());

        SharedPreferences sharedPreferences = activity.getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE);
        String km = getDistanceInKmString(branch.getLatLng(), new LatLng(Double.parseDouble(sharedPreferences.getString(Shared.KEY_LATITUDE, "0")),
                Double.parseDouble(sharedPreferences.getString(Shared.KEY_LONGITUDE, "0"))));
        holder.tvDistance.setText(km);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, RestaurantActivity.class);
                intent.putExtra("id", branch.getId());
                intent.putExtra("name", branch.getName());
                intent.putExtra("phone", branch.getPhoneNumber());
                intent.putExtra("address", branch.getAddress());
                intent.putExtra("imgURL", branch.getImageUrl());
                intent.putExtra("openTime", branch.getOpeningTime());
                intent.putExtra("closeTime", branch.getClosingTime());
                intent.putExtra("isSell", branch.isSell());
                intent.putExtra("lat", branch.getLatLng().latitude);
                intent.putExtra("lng", branch.getLatLng().longitude);
                activity.startActivity(intent);
            }
        });
    }
    public double rad(double x){
        return x * Math.PI / 180;
    }
    public double getDistance(LatLng p1, LatLng p2){
        double R = 6378137; // Earth’s mean radius in meter
        double dLat = rad(p2.latitude - p1.latitude);
        double dLng = rad(p2.longitude - p1.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(rad(p1.latitude)) * Math.cos(rad(p2.latitude)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    @SuppressLint("DefaultLocale")
    public String getDistanceInKmString(LatLng p1, LatLng p2){
        return String.format("%.1f", getDistance(p1, p2)/1000) + "Km";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivBranch;
        public TextView tvBranchName;
        public TextView tvBranchAddress;
        public TextView tvDistance;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBranch = itemView.findViewById(R.id.ivBranch);
            tvBranchName = itemView.findViewById(R.id.tvBranchName);
            tvBranchAddress = itemView.findViewById(R.id.tvBranchAddress);
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
}
