package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.MaterialToolbar;
import com.luanvan.customer.Adapter.RecyclerViewBranchAdapter;
import com.luanvan.customer.components.Branch;
import com.luanvan.customer.components.ResultsCode;
import com.luanvan.customer.components.Shared;
import com.luanvan.customer.components.SortPlaces;

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
import java.util.Collections;

public class BranchActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;

    private ArrayList<Branch> branches = new ArrayList<>();
    private String token;
    private LatLng currentLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch);

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        layoutProgressBar = findViewById(R.id.layoutProgressBar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        SharedPreferences sharedPreferences = getSharedPreferences(Shared.TOKEN, MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "");

        sharedPreferences = getSharedPreferences(Shared.CONSUMER, MODE_PRIVATE);
        currentLocation = new LatLng(Double.parseDouble(sharedPreferences.getString(Shared.KEY_LATITUDE, "0")),
                Double.parseDouble(sharedPreferences.getString(Shared.KEY_LONGITUDE, "0")));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250, 250);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        layoutProgressBar.addView(progressBar, params);

        new GetBranch().get(1);
    }

    @SuppressLint("StaticFieldLeak")
    class GetBranch extends AsyncTask<String,String,String> {
        private InputStream is;
        private int resultCode;
        private final String branchURL = "https://orefoo.herokuapp.com/branch?page=";

        private int page;
        public void get(int page){
            this.page = page;
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
                URL url = new URL(branchURL + page);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", token);
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.connect();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+"");
                if (statusCode >= 200 && statusCode < 400){
                    resultCode = ResultsCode.SUCCESS;
                    is = connection.getInputStream();
                } else {
                    resultCode = ResultsCode.FAILED;
                    is = connection.getErrorStream();
                }

                reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                    Log.d("ResponseGetBranch: ", "> " + line);
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

            progressBar.setVisibility(View.INVISIBLE);

            if (s == null) return;

            switch (resultCode) {
                case ResultsCode.SUCCESS:
                    try {
                        branches.clear();
                        // add branches
                        JSONArray jsonArray = new JSONArray(s);
                        for (int i = 0; i < Math.min(jsonArray.length(), 5); i++){
                            final JSONObject jsonObject = jsonArray.getJSONObject(i);
                            branches.add(new Branch(jsonObject.getInt("id"), jsonObject.getString("name"), jsonObject.getString("phoneNumber"),
                                    jsonObject.getString("imageUrl"), jsonObject.getString("openingTime"), jsonObject.getString("closingTime"),
                                    jsonObject.getString("address"), new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")),
                                    jsonObject.getBoolean("isSell"), jsonObject.getInt("merchant"), jsonObject.getString("branchStatus")));
                        }

                        // sort branch following distance
                        if (currentLocation != null) Collections.sort(branches, new SortPlaces(currentLocation));
                        else {
                            Toast.makeText(BranchActivity.this, "Cannot get current location, try again", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // show branch
                        recyclerView.setAdapter(new RecyclerViewBranchAdapter(BranchActivity.this, branches));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(BranchActivity.this, getResources().getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
                    break;
                case ResultsCode.FAILED:
                    Toast.makeText(BranchActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                    break;
                case ResultsCode.IO_EXCEPTION:
                    Toast.makeText(BranchActivity.this, getResources().getString(R.string.io_exception), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}