package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.MaterialToolbar;
import com.luanvan.customer.Adapter.RecyclerViewCategoryAdapter;
import com.luanvan.customer.components.Branch;
import com.luanvan.customer.components.Category;
import com.luanvan.customer.components.RequestUrl;
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

public class CategoryActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;

    private ArrayList<Category> categories = new ArrayList<>();
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

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

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        layoutProgressBar.addView(progressBar, params);

        new GetCategory().execute();
    }

    @SuppressLint("StaticFieldLeak")
    class GetCategory extends AsyncTask<String,String,String> {
        private InputStream is;
        private int resultCode;

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
                URL url = new URL(RequestUrl.CATEGORY);
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
                    Log.d("ResponseGetCategory: ", "> " + line);
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

            switch (resultCode) {
                case ResultsCode.SUCCESS:
                    try {
                        categories.clear();

                        JSONArray jsonArray = new JSONArray(s);
                        for (int index = 0; index < jsonArray.length(); index++){
                            JSONObject jsonObject = jsonArray.getJSONObject(index);
                            categories.add(new Category(jsonObject.getInt("id"), jsonObject.getString("name"), jsonObject.getString("imageUrl")));
                        }

                        recyclerView.setAdapter(new RecyclerViewCategoryAdapter(CategoryActivity.this, categories));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(CategoryActivity.this, getResources().getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
                    break;
                case ResultsCode.FAILED:
                    Toast.makeText(CategoryActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                    break;
                case ResultsCode.IO_EXCEPTION:
                    Toast.makeText(CategoryActivity.this, getResources().getString(R.string.io_exception), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}