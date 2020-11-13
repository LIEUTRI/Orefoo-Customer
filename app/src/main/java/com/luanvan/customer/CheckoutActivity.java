package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.luanvan.customer.Adapter.RecyclerViewCartItemAdapter;
import com.luanvan.customer.Adapter.RecyclerViewVictualAdapter;
import com.luanvan.customer.components.CartDialog;
import com.luanvan.customer.components.CartItem;
import com.luanvan.customer.components.ResultsCode;
import com.luanvan.customer.components.Shared;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class CheckoutActivity extends AppCompatActivity {

    private TextView tvConsumerName;
    private TextView tvPhone;
    private TextView tvAddress;
    private TextView tvBranchName;
    private TextView tvDeliveryTime;
    private TextView btnConfirmOrder;
    private RecyclerView recyclerView;
    private String token;
    private int cartId;
    private String branchName;
    private String consumerAddress;
    private double consumerLat, consumerLng;
    private double distance;

    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;

    private ArrayList<CartItem> cartItems = new ArrayList<>();
    public static double totalPriceVictuals = 0.0;
    public static double shippingFee = 0.0;

    @SuppressLint("StaticFieldLeak")
    public static TextView tvTotalVictuals;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvTotal;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvTotalFinal;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvShipFee;
    @SuppressLint("SetTextI18n")
    public static int portion = 0;
    @SuppressLint("SetTextI18n")
    private TextView tvDeliveryFee;
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        tvConsumerName = findViewById(R.id.tvConsumerName);
        tvPhone = findViewById(R.id.tvPhoneNumber);
        tvAddress = findViewById(R.id.tvAddress);
        tvBranchName = findViewById(R.id.tvBranchName);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        recyclerView = findViewById(R.id.recyclerView);
        layoutProgressBar = findViewById(R.id.layoutProgressBar);
        tvTotalFinal = findViewById(R.id.tvTotalFinal);
        tvShipFee = findViewById(R.id.tvShipFee);
        tvTotal = findViewById(R.id.tvTotal);
        tvTotalVictuals = findViewById(R.id.tvTotalVictuals);
        tvDeliveryTime = findViewById(R.id.tvDeliveryTime);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);

        SharedPreferences sharedPreferences = getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "");

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        layoutProgressBar.addView(progressBar, params);
        progressBar.setVisibility(View.VISIBLE);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        sharedPreferences = getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE);
        tvConsumerName.setText(sharedPreferences.getString(Shared.KEY_LAST_NAME,"")+" "+sharedPreferences.getString(Shared.KEY_FIRST_NAME, ""));
        tvPhone.setText(sharedPreferences.getString(Shared.KEY_PHONE,""));
        tvAddress.setText(sharedPreferences.getString(Shared.KEY_ADDRESS,""));
        consumerAddress = sharedPreferences.getString(Shared.KEY_ADDRESS, "");
        consumerLat = Double.parseDouble(sharedPreferences.getString(Shared.KEY_LATITUDE, "0"));
        consumerLng = Double.parseDouble(sharedPreferences.getString(Shared.KEY_LONGITUDE, "0"));

        sharedPreferences = getSharedPreferences(Shared.BRANCH, Context.MODE_PRIVATE);
        branchName = sharedPreferences.getString(Shared.KEY_BRANCH_NAME, "");
        distance = sharedPreferences.getFloat(Shared.KEY_BRANCH_DISTANCE, 0);
        distance = round(distance, 1);
        tvBranchName.setText(branchName);
        tvDeliveryFee.setText(getResources().getString(R.string.shipping_fee)+" ("+distance+"km)");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, (int)(5 * distance + 15));
        String dateTime = calendar.getTime().toString();
        tvDeliveryTime.setText(dateTime);

        sharedPreferences = getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE);
        int consumerId = sharedPreferences.getInt(Shared.KEY_CONSUMER_ID, -1);

        sharedPreferences = getSharedPreferences(Shared.CART, Context.MODE_PRIVATE);
        cartId = sharedPreferences.getInt(Shared.KEY_CART_ID, -1);

        new GetCartTask().execute(consumerId+"");

        btnConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonOrder = new JSONObject();
                try {
                    jsonOrder.put("address", consumerAddress);
                    jsonOrder.put("latitude", consumerLat);
                    jsonOrder.put("longitude", consumerLng);

                    new OrderTask().confirmOrder(cartId, jsonOrder);

                    btnConfirmOrder.setEnabled(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static double round(double value, int places){
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        bigDecimal = bigDecimal.setScale(places, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    @SuppressLint("StaticFieldLeak")
    class GetCartTask extends AsyncTask<String,String,String> {
        private InputStream is;
        private final String cartURL = "https://orefoo.herokuapp.com/cart?consumer-id=";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(cartURL + strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", token);
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.connect();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+"");
                if (statusCode >= 200 && statusCode < 400){
                    is = connection.getInputStream();
                } else {
                    is = connection.getErrorStream();
                }

                reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                    Log.d("ResponseGetCartTask: ", "> " + line);
                }

                return buffer.toString();
            } catch (SocketTimeoutException e) {
                Log.i("CheckoutActivity", Objects.requireNonNull(e.getMessage()));
            } catch (IOException e){
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s == null) return;
            progressBar.setVisibility(View.INVISIBLE);

            totalPriceVictuals = 0.0;
            int quantity = 0;
            try {
                JSONObject jsonObject = new JSONObject(s);
                double totalPrice = jsonObject.getDouble("totalPrice");
                shippingFee = jsonObject.getDouble("shippingFee");
                totalPriceVictuals = totalPrice;
                int branchId = jsonObject.getInt("branch");
                JSONArray jsonArray = jsonObject.getJSONArray("cartItemsCollection");

                for (int i=0; i<jsonArray.length(); i++){
                    JSONObject item = jsonArray.getJSONObject(i);
                    quantity += item.getInt("quantity");
                    cartItems.add(new CartItem(item.getInt("id"), item.getString("name"), item.getString("imageUrl"),
                            item.getDouble("price"), item.getDouble("discount"), item.getInt("quantity"), item.getInt("cart"),
                            item.getInt("victuals")));
                }

                portion = quantity;
                tvTotalVictuals.setText(getResources().getString(R.string.sum)+" ("+quantity+" "+getResources().getString(R.string.portion)+")");
                tvTotal.setText(String.format("%,.0f", totalPriceVictuals)+"đ");
                tvShipFee.setText(String.format("%,.0f", shippingFee)+"đ");
                tvTotalFinal.setText(String.format("%,.0f",totalPriceVictuals+shippingFee)+"đ");

                recyclerView.setAdapter(new RecyclerViewCartItemAdapter(CheckoutActivity.this, cartItems));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class OrderTask extends AsyncTask<String,String,String> {
        private InputStream is;
        private OutputStream os;
        private final String orderUrl = "https://orefoo.herokuapp.com/order?cart-id=";
        private int cartId;
        private JSONObject jsonBranch;

        public void confirmOrder(int cartId, JSONObject jsonBranch){
            this.cartId = cartId;
            this.jsonBranch = jsonBranch;
            execute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(orderUrl + cartId);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", token);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.getDoOutput();
                connection.connect();

                os = new BufferedOutputStream(connection.getOutputStream());
                os.write(jsonBranch.toString().getBytes());
                os.flush();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+"");
                if (statusCode >= 200 && statusCode < 400){
                    Log.i("result", "confirm order success");
                    return "200";
                } else {
                    Log.i("result", "confirm order failed");
                    return "failed";
                }
            } catch (SocketTimeoutException e) {
                Log.i("MenuFragment", e.getMessage());
            } catch (IOException e){
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.INVISIBLE);
            btnConfirmOrder.setEnabled(true);
            if (s == null) return;
            if (s.equals("200")){
                Toast.makeText(CheckoutActivity.this, getResources().getString(R.string.order_success), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(CheckoutActivity.this, getResources().getString(R.string.order_failed), Toast.LENGTH_LONG).show();
            }
        }
    }
}