package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
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
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

public class CheckoutActivity extends AppCompatActivity {

    private TextView tvConsumerName;
    private TextView tvPhone;
    private TextView tvAddress;
    private TextView tvBranchName;
    private TextView tvPay;
    private RecyclerView recyclerView;
    private String token;

    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;

    private ArrayList<CartItem> cartItems = new ArrayList<>();
    public static double totalPriceVictuals = 0.0;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvTotal;
    private TextView tvTotalFinal;
    private TextView tvShipFee;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        tvConsumerName = findViewById(R.id.tvConsumerName);
        tvPhone = findViewById(R.id.tvPhoneNumber);
        tvAddress = findViewById(R.id.tvAddress);
        tvBranchName = findViewById(R.id.tvBranchName);
        tvPay = findViewById(R.id.tvPay);
        recyclerView = findViewById(R.id.recyclerView);
        layoutProgressBar = findViewById(R.id.layoutProgressBar);
        tvTotalFinal = findViewById(R.id.tvTotalFinal);
        tvShipFee = findViewById(R.id.tvShipFee);
        tvTotal = findViewById(R.id.tvTotal);

        SharedPreferences sharedPreferences = getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "");
        Log.i("token", token);

        sharedPreferences = getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        layoutProgressBar.addView(progressBar, params);
        progressBar.setVisibility(View.VISIBLE);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        tvConsumerName.setText(sharedPreferences.getString(Shared.KEY_LAST_NAME,"")+" "+sharedPreferences.getString(Shared.KEY_FIRST_NAME, ""));
        tvPhone.setText(sharedPreferences.getString(Shared.KEY_PHONE,""));
        tvAddress.setText(sharedPreferences.getString(Shared.KEY_ADDRESS,""));

        sharedPreferences = getSharedPreferences(Shared.CART, Context.MODE_PRIVATE);
        int cartId = sharedPreferences.getInt(Shared.KEY_CART_ID, -1);

        new CartItemTask().getAllItem(cartId);
    }

    @SuppressLint("StaticFieldLeak")
    class CartItemTask extends AsyncTask<String,String,String> {
        private InputStream is;
        private int cartID;
        private final String cartItemURL = "https://orefoo.herokuapp.com/cart-item?cart-id=";
        private int resultCode;

        public void getAllItem(int cartID){
            this.cartID = cartID;
            execute();
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(cartItemURL + cartID);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", token);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.connect();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+"");

                if (statusCode >= 200 && statusCode < 400){
                    is = connection.getInputStream();
                    resultCode = ResultsCode.SUCCESS;
                } else {
                    is = connection.getErrorStream();
                    if (statusCode == 406) {
                        resultCode = ResultsCode.DIFFERENCE_BRANCH;
                    } else resultCode = ResultsCode.FAILED;
                }

                reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                    Log.d("ResponseCartItem: ", "> " + line);
                }

                return buffer.toString();
            } catch (SocketTimeoutException e) {
                resultCode = ResultsCode.SOCKET_TIMEOUT;
            } catch (IOException e){
                resultCode = ResultsCode.IO_EXCEPTION;
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

        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressBar.setVisibility(View.INVISIBLE);

            if (s == null) return;

            switch (resultCode) {
                case ResultsCode.SUCCESS:
                    Log.i("result", "get cart item success");
                    try {
                        totalPriceVictuals = 0.0;
                        JSONArray jsonArray = new JSONArray(s);
                        for (int i=0; i<jsonArray.length(); i++){
                            JSONObject item = jsonArray.getJSONObject(i);
                            totalPriceVictuals += item.getInt("price")-item.getInt("discount");
                            cartItems.add(new CartItem(item.getInt("id"), item.getString("name"), item.getString("imageUrl"),
                                    item.getDouble("price"), item.getDouble("discount"), item.getInt("quantity"), item.getInt("cart"),
                                    item.getInt("victuals")));
                        }

                        tvTotal.setText(String.format("%,.0f", totalPriceVictuals)+"đ");

                        recyclerView.setAdapter(new RecyclerViewCartItemAdapter(CheckoutActivity.this, cartItems));
                        Log.i("jsonCartItem", jsonArray.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case ResultsCode.DIFFERENCE_BRANCH:
                    break;
                case ResultsCode.FAILED:
                    Log.i("result", "get failed");
                    break;
                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(CheckoutActivity.this, getResources().getString(R.string.socket_timeout), Toast.LENGTH_SHORT).show();
                    break;
                case ResultsCode.IO_EXCEPTION:
                    Toast.makeText(CheckoutActivity.this, "IO Exception", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }
}