package com.luanvan.customer.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.luanvan.customer.Adapter.RecyclerViewOngoingAdapter;
import com.luanvan.customer.R;
import com.luanvan.customer.components.Branch;
import com.luanvan.customer.components.CartItem;
import com.luanvan.customer.components.Order;
import com.luanvan.customer.components.RequestUrl;
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

public class OngoingFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<Order> orders = new ArrayList<>();
    private SwipeRefreshLayout layoutRefresh;

    private String token = "";
    private int consumerId;

    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;

    public OngoingFragment() { }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ongoing, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "")+"";

        sharedPreferences = getActivity().getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE);
        consumerId = sharedPreferences.getInt(Shared.KEY_CONSUMER_ID, -1);
        Log.i("token", token);
        Log.i("consumerId", consumerId+"");

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleSmall);
        layoutProgressBar.addView(progressBar, params);

        layoutRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new OrderTask().get(consumerId);
            }
        });

        new OrderTask().get(consumerId);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        layoutProgressBar = view.findViewById(R.id.layoutProgressBar);
        layoutRefresh = view.findViewById(R.id.layoutRefresh);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @SuppressLint("StaticFieldLeak")
    class OrderTask extends AsyncTask<String,String,String> {
        private InputStream is;
        private int resultCode;
        private int consumerId;

        public void get(int consumerId){
            this.consumerId = consumerId;
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
                URL url = new URL(RequestUrl.ORDER +"?consumer-id="+ consumerId);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", token);
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
                    Log.d("ResponseOrderTask: ", "> " + line);
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.INVISIBLE);
            layoutRefresh.setRefreshing(false);
            if (s == null) return;

            switch (resultCode) {
                case ResultsCode.SUCCESS:
                    Log.i("result", "update success");

                    orders.clear();
                    try {
                        JSONArray jsonArray = new JSONArray(s);
                        for (int index=0; index < jsonArray.length(); index++){
                            JSONObject jsonObject = jsonArray.getJSONObject(index);
                            JSONArray orderItems = jsonObject.getJSONArray("orderItems");
                            ArrayList<CartItem> listOrderItems = new ArrayList<>();
                            for (int i=0; i<orderItems.length(); i++){
                                JSONObject item = orderItems.getJSONObject(i);
                                listOrderItems.add(new CartItem(item.getInt("id"), item.getString("name"), item.getString("imageUrl"),
                                        item.getDouble("price"), item.getDouble("discount"), item.getInt("quantity"),
                                        item.getInt("order"), item.getInt("victuals")));
                            }
                            if (jsonObject.getString("orderStatus").equals("ordered") || jsonObject.getString("orderStatus").equals("accepted")
                                || jsonObject.getString("orderStatus").equals("picked")){
                                orders.add(new Order(jsonObject.getInt("id"), jsonObject.getDouble("totalPay"), jsonObject.getDouble("victualsPrice"),
                                        jsonObject.getDouble("shippingFee"), jsonObject.getString("shippingAddress"), jsonObject.getString("note"), jsonObject.getString("time"),
                                        new Branch(jsonObject.getJSONObject("branch").getInt("id"), jsonObject.getJSONObject("branch").getString("name"), jsonObject.getJSONObject("branch").getString("imageUrl")),
                                        jsonObject.getInt("consumer"), jsonObject.getString("shipper").equals("null") ? -1:jsonObject.getInt("shipper"),
                                        jsonObject.getString("orderStatus"), listOrderItems));
                            }
                        }

                        recyclerView.setAdapter(new RecyclerViewOngoingAdapter(getActivity(), orders));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case ResultsCode.FAILED:
                    Log.i("result", "update failed");
                    break;
                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.socket_timeout), Toast.LENGTH_SHORT).show();
                    break;
                case ResultsCode.IO_EXCEPTION:
                    Toast.makeText(getActivity(), "IO Exception", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}