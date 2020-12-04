package com.luanvan.customer.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.luanvan.customer.Adapter.RecyclerViewVictualAdapter;
import com.luanvan.customer.CheckoutActivity;
import com.luanvan.customer.R;
import com.luanvan.customer.components.CartDialog;
import com.luanvan.customer.components.ResultsCode;
import com.luanvan.customer.components.Shared;
import com.luanvan.customer.components.Victual;

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
import java.util.Objects;

public class MenuFragment extends Fragment {

    private String token = "";
    private int id;
    private int cartId;
    private ArrayList<Victual> victuals;
    private RecyclerView recyclerView;
    private TextView tvTotal, tvTotalOrigin;
    private TextView btnCheckout;
    private RelativeLayout layoutCartBar;
    public static BottomSheetBehavior bottomSheetBehavior;
    private CoordinatorLayout coordinatorLayout;
    public static MutableLiveData<Double> totalPrice = new MutableLiveData<>();
    public static MutableLiveData<Double> totalPriceOrigin = new MutableLiveData<>();

    private RelativeLayout layoutProgressBar;
    @SuppressLint("StaticFieldLeak")
    public static ProgressBar progressBar;
    public MenuFragment() { }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        coordinatorLayout = view.findViewById(R.id.bottomSheetCoordinatorLayout);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvTotalOrigin = view.findViewById(R.id.tvTotalOrigin);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        layoutCartBar = view.findViewById(R.id.layoutTotal);
        layoutProgressBar = view.findViewById(R.id.layoutProgressBar);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "")+"";

        sharedPreferences = getActivity().getSharedPreferences(Shared.CART, Context.MODE_PRIVATE);
        cartId = sharedPreferences.getInt(Shared.KEY_CART_ID, -1);

        id = getActivity().getIntent().getIntExtra("id", -1);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleSmall);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250, 250);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutProgressBar.addView(progressBar, params);
        progressBar.setVisibility(View.INVISIBLE);

        victuals = new ArrayList<>();

        bottomSheetBehavior = BottomSheetBehavior.from(coordinatorLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setDraggable(true);

        totalPrice.setValue(0.0);
        totalPrice.observe(getActivity(), new Observer<Double>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(Double d) {
                if (totalPrice.getValue() == 0.0){
                    CloseBottomSheet();
                } else {
                    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) OpenBottomSheet();
                }
                @SuppressLint("DefaultLocale")
                String total = String.format("%,.0f", totalPrice.getValue())+"đ";
                tvTotal.setText(total);
            }
        });

        totalPriceOrigin.setValue(0.0);
        totalPriceOrigin.observe(getActivity(), new Observer<Double>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(Double d) {
                @SuppressLint("DefaultLocale")
                String total = totalPrice.getValue().equals(totalPriceOrigin.getValue()) ? "":String.format("%,.0f", totalPriceOrigin.getValue())+"đ";
                tvTotalOrigin.setText(total);
            }
        });

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CheckoutActivity.class);
                intent.putExtra("cartItem", getCart());
                startActivity(intent);
            }
        });

        layoutCartBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartDialog cartDialog = new CartDialog(Objects.requireNonNull(getActivity()), R.style.CartDialog);
                cartDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                cartDialog.show();
            }
        });
    }
    public static void OpenBottomSheet(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    public static void CloseBottomSheet(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private String getCart(){
        SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Shared.CART, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Shared.KEY_CART_ITEM, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (id > 0){
            new GetVictuals().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class GetVictuals extends AsyncTask<String,String,String> {
        private InputStream is;
        private final String victualsURL = "https://orefoo.herokuapp.com/victuals?branch-id=" + id;

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
                URL url = new URL(victualsURL);
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
                    Log.d("ResponseGetVictuals: ", "> " + line);
                }

                return buffer.toString();
            } catch (SocketTimeoutException e) {
                Log.i("MenuFragment", Objects.requireNonNull(e.getMessage()));
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressBar.setVisibility(View.INVISIBLE);

            if (s == null) return;

            victuals.clear();
            try {
                JSONArray jsonArray = new JSONArray(s);
                for (int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONArray categories = jsonObject.getJSONArray("categories");

                    victuals.add(new Victual(jsonObject.getString("id"), jsonObject.getString("name"), jsonObject.getString("price"),
                            jsonObject.getString("discount"), jsonObject.getString("createdAt"), jsonObject.getString("updatedAt"),
                            jsonObject.getString("imageUrl"), jsonObject.getBoolean("isSell"), jsonObject.getString("branch"), categories,0,-1));

                    new CartItemTask().getAllItem(cartId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    ///////////////////
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

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) return;

            switch (resultCode) {
                case ResultsCode.SUCCESS:
                    Log.i("result", "get cart item success");
                    try {
                        double totalPriceVictuals = 0.0;
                        double totalPriceVictualsOrigin = 0.0;
                        JSONArray jsonArray = new JSONArray(s);
                        for (int i=0; i<jsonArray.length(); i++){
                            JSONObject item = jsonArray.getJSONObject(i);
                            totalPriceVictuals += item.getInt("price")-item.getInt("discount");
                            totalPriceVictualsOrigin += item.getInt("price");
                            for (int index=0; index<victuals.size(); index++){
                                if (item.getInt("victuals") == Integer.parseInt(victuals.get(index).getId())){
                                    victuals.get(index).setQuantity(item.getInt("quantity"));
                                    victuals.get(index).setCartItemId(item.getInt("id"));
                                }
                            }
                        }

                        // update total price
                        totalPrice.setValue(totalPriceVictuals);
                        totalPriceOrigin.setValue(totalPriceVictualsOrigin);

                        @SuppressLint("DefaultLocale")
                        String p1 = String.format("%,.0f", totalPriceVictuals)+"đ";
                        @SuppressLint("DefaultLocale")
                        String p2 = totalPriceVictuals==totalPriceVictualsOrigin ? "":String.format("%,.0f", totalPriceVictualsOrigin)+"đ";
                        tvTotal.setText(p1);
                        tvTotalOrigin.setPaintFlags(tvTotalOrigin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        tvTotalOrigin.setText(p2);
                        Log.i("jsonCartItem", jsonArray.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case ResultsCode.DIFFERENCE_BRANCH:
                    break;
                case ResultsCode.FAILED:
                    Log.i("result", "get failed (cart have no items)");
                    CloseBottomSheet();
                    break;
                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.socket_timeout), Toast.LENGTH_SHORT).show();
                    break;
                case ResultsCode.IO_EXCEPTION:
                    Toast.makeText(getActivity(), "IO Exception", Toast.LENGTH_SHORT).show();
                    break;
            }

            recyclerView.setAdapter(new RecyclerViewVictualAdapter(getActivity(), victuals));
        }
    }
}