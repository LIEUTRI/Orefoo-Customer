package com.luanvan.customer.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.luanvan.customer.CheckoutActivity;
import com.luanvan.customer.R;
import com.luanvan.customer.components.Branch;
import com.luanvan.customer.components.CartDialog;
import com.luanvan.customer.components.CartItem;
import com.luanvan.customer.components.Order;
import com.luanvan.customer.components.ResultsCode;
import com.luanvan.customer.components.Shared;
import com.luanvan.customer.components.SortPlaces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class RecyclerViewOngoingAdapter extends RecyclerView.Adapter<RecyclerViewOngoingAdapter.ViewHolder>{
    private List<Order> list;
    private Activity activity;
    private String token;
    public RecyclerViewOngoingAdapter(Activity activity, List<Order> list){
        this.activity = activity;
        this.list = list;
    }
    @NonNull
    @Override
    public RecyclerViewOngoingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        SharedPreferences sharedPreferences = activity.getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "");

        View view = LayoutInflater.from(activity).inflate(R.layout.order, parent, false);
        return new RecyclerViewOngoingAdapter.ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewOngoingAdapter.ViewHolder holder, final int position) {
        final Order order = list.get(position);

        holder.tvBranchName.setText(order.getBranch().getName());
        Calendar calendar = Calendar.getInstance();
        Timestamp timestamp = Timestamp.valueOf(order.getTime().substring(0, order.getTime().indexOf("+")).replace("T", " "));
        calendar.setTime(timestamp);
        calendar.add(Calendar.HOUR_OF_DAY, 7);
        holder.tvTime.setText(activity.getResources().getString(R.string.order_at)+calendar.getTime().toString().substring(0, calendar.getTime().toString().indexOf("GMT")-1));
        holder.tvVictualsSize.setText(order.getOrderItems().size()+" "+activity.getResources().getString(R.string.dish));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvBranchName;
        public TextView tvTime;
        public TextView tvVictualsSize;
        public ImageView ivBranch;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBranch = itemView.findViewById(R.id.ivBranch);
            tvBranchName = itemView.findViewById(R.id.tvBranchName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvVictualsSize = itemView.findViewById(R.id.tvVictualsSize);
        }
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
}
