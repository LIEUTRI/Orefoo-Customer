package com.luanvan.customer.components;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.luanvan.customer.Adapter.RecyclerViewCartItemAdapter;
import com.luanvan.customer.CheckoutActivity;
import com.luanvan.customer.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

public class CartDialog extends Dialog {

    private ImageButton btnClose;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvTotal, tvTotalOrigin;
    private TextView btnCheckout;
    private String token;
    private int cartId;
    private ArrayList<CartItem> cartItems = new ArrayList<>();
    private RecyclerView recyclerView;
    private Activity activity;

    public static double totalPriceVictuals = 0.0;
    public static double totalPriceVictualsOrigin = 0.0;

    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;
    public CartDialog(@NonNull Context context) {
        super(context);
    }

    public CartDialog(@NonNull Activity activity, int themeResId) {
        super(activity, themeResId);
        this.activity = activity;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_dialog);

        btnClose = findViewById(R.id.btnClose);
        recyclerView = findViewById(R.id.recyclerView);
        tvTotal = findViewById(R.id.tvTotal);
        tvTotalOrigin = findViewById(R.id.tvTotalOrigin);
        layoutProgressBar = findViewById(R.id.layoutProgressBar);
        btnCheckout = findViewById(R.id.btnCheckout);

        progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleSmall);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250, 250);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutProgressBar.addView(progressBar, params);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        setCanceledOnTouchOutside(false);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(activity, CheckoutActivity.class));
                cancel();
            }
        });

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "");

        sharedPreferences = getContext().getSharedPreferences(Shared.CART, Context.MODE_PRIVATE);
        cartId = sharedPreferences.getInt(Shared.KEY_CART_ID, -1);

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
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
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
                        totalPriceVictualsOrigin = 0.0;
                        JSONArray jsonArray = new JSONArray(s);
                        for (int i=0; i<jsonArray.length(); i++){
                            JSONObject item = jsonArray.getJSONObject(i);
                            totalPriceVictuals += item.getInt("price")-item.getInt("discount");
                            totalPriceVictualsOrigin += item.getInt("price");
                            cartItems.add(new CartItem(item.getInt("id"), item.getString("name"), item.getString("imageUrl"),
                                    item.getDouble("price"), item.getDouble("discount"), item.getInt("quantity"), item.getInt("cart"),
                                    item.getInt("victuals")));
                        }


                        CartDialog.tvTotal.setText(String.format("%,.0f", totalPriceVictuals)+"đ");
                        CartDialog.tvTotalOrigin.setPaintFlags(tvTotalOrigin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        CartDialog.tvTotalOrigin.setText(totalPriceVictualsOrigin==totalPriceVictuals ? "":String.format("%,.0f", totalPriceVictualsOrigin)+"đ");

                        recyclerView.setAdapter(new RecyclerViewCartItemAdapter(activity, cartItems));
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
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.socket_timeout), Toast.LENGTH_SHORT).show();
                    break;
                case ResultsCode.IO_EXCEPTION:
                    Toast.makeText(getContext(), "IO Exception", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }
}
