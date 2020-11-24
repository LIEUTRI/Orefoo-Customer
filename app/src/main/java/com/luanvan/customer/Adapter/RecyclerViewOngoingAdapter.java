package com.luanvan.customer.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.luanvan.customer.TrackShipperActivity;
import com.luanvan.customer.components.Branch;
import com.luanvan.customer.components.CartDialog;
import com.luanvan.customer.components.CartItem;
import com.luanvan.customer.components.Order;
import com.luanvan.customer.components.ResultsCode;
import com.luanvan.customer.components.Shared;
import com.luanvan.customer.components.SortPlaces;
import com.luanvan.customer.components.Victual;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RecyclerViewOngoingAdapter extends RecyclerView.Adapter<RecyclerViewOngoingAdapter.ViewHolder>{
    private List<Order> list;
    private Activity activity;
    private String token;
    public final String TAG = "OngoingAdapter";
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

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.image_not_found);
        Glide.with(activity).load(order.getBranch().getImageUrl()).apply(options).into(holder.ivBranch);

        holder.tvBranchName.setText(order.getBranch().getName());

        Calendar calendar = Calendar.getInstance();
        Timestamp timestamp = Timestamp.valueOf(order.getTime().substring(0, order.getTime().indexOf("+")).replace("T", " "));
        calendar.setTime(timestamp);
        calendar.add(Calendar.HOUR_OF_DAY, 7);
        holder.tvTime.setText(activity.getResources().getString(R.string.order_at)+"\n"+formatTime(calendar.getTime(), new Locale("vi", "VN")));

        holder.tvVictualsSize.setText(order.getOrderItems().size()+" "+activity.getResources().getString(R.string.dish));
        switch (order.getOrderStatus()) {
            case "ordered":
                holder.tvStatus.setText(activity.getString(R.string.ordered));
                holder.ivStatus.setImageDrawable(activity.getDrawable(R.drawable.ic_hourglass_top_24));
                break;
            case "accepted":
                holder.tvStatus.setText(activity.getString(R.string.accepted));
                holder.ivStatus.setImageDrawable(activity.getDrawable(R.drawable.ic_done_24));
                break;
            case "picked":
                holder.tvStatus.setText(activity.getString(R.string.picked));
                holder.ivStatus.setImageDrawable(activity.getDrawable(R.drawable.ic_pedal_bike_24));
                break;
            case "success":
                holder.tvStatus.setText(activity.getString(R.string.success));
                holder.ivStatus.setImageDrawable(activity.getDrawable(R.drawable.ic_done_all_24));
                break;
            case "consumer_canceled":
                holder.tvStatus.setText(activity.getString(R.string.consumer_canceled));
                holder.ivStatus.setImageDrawable(activity.getDrawable(R.drawable.ic_cancel_24));
                break;
            case "merchant_canceled":
                holder.tvStatus.setText(activity.getString(R.string.merchant_canceled));
                holder.ivStatus.setImageDrawable(activity.getDrawable(R.drawable.ic_cancel_24));
                break;
            case "shipper_canceled":
                holder.tvStatus.setText(activity.getString(R.string.shipper_canceled));
                holder.ivStatus.setImageDrawable(activity.getDrawable(R.drawable.ic_cancel_24));
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Victual> victuals = new ArrayList<>();
                for (int index=0; index<order.getOrderItems().size(); index++){
                    victuals.add(new Victual(order.getOrderItems().get(index).getName(), order.getOrderItems().get(index).getImageUrl(),
                            order.getOrderItems().get(index).getQuantity(), order.getOrderItems().get(index).getPrice()+"", order.getOrderItems().get(index).getDiscount()+""));
                }
                Intent intent = new Intent(activity, TrackShipperActivity.class);
                intent.putExtra("victuals", victuals);
                intent.putExtra("shipperId", order.getShipper());
                activity.startActivity(intent);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvBranchName;
        public TextView tvTime;
        public TextView tvVictualsSize;
        public TextView tvStatus;
        public ImageView ivStatus;
        public ImageView ivBranch;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBranch = itemView.findViewById(R.id.ivBranch);
            tvBranchName = itemView.findViewById(R.id.tvBranchName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvVictualsSize = itemView.findViewById(R.id.tvVictualsSize);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivStatus = itemView.findViewById(R.id.ivStatus);
        }
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    public static String formatTime(Date time, Locale locale){
        String timeFormat = "HH:mm dd MMMM, yyyy";

        SimpleDateFormat formatter;

        try {
            formatter = new SimpleDateFormat(timeFormat, locale);
        } catch(Exception e) {
            formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", locale);
        }
        return formatter.format(time);
    }
}
