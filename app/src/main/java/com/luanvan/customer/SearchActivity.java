package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.luanvan.customer.Adapter.RecyclerViewBranchAdapter;
import com.luanvan.customer.components.Branch;
import com.luanvan.customer.components.ConnectionStateMonitor;
import com.luanvan.customer.components.NetworkState;
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
import java.util.Collections;

public class SearchActivity extends AppCompatActivity {

    private ImageButton ibBack;
    private EditText etSearch;
    private RecyclerView recyclerView;
    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;

    private ArrayList<Branch> branches = new ArrayList<>();
    private String token;
    private LatLng currentLocation;
    private SearchBranchTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ibBack = findViewById(R.id.ibBack);
        etSearch = findViewById(R.id.etSearch);
        recyclerView = findViewById(R.id.recyclerView);
        layoutProgressBar = findViewById(R.id.layoutProgressBar);

        // show keyboard input as default //////////////////////////////////////////////////////////////////////////
        etSearch.requestFocus();
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        // set up to hide keyboard /////////////////////////////////////////////////////////////////////////////////
        setupUI(findViewById(R.id.layoutSearch));
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250, 250);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        layoutProgressBar.addView(progressBar, params);
        progressBar.setVisibility(View.INVISIBLE);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        SharedPreferences sharedPreferences = getSharedPreferences(Shared.TOKEN, MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "");

        sharedPreferences = getSharedPreferences(Shared.CONSUMER, MODE_PRIVATE);
        currentLocation = new LatLng(Double.parseDouble(sharedPreferences.getString(Shared.KEY_LATITUDE, "0")),
                Double.parseDouble(sharedPreferences.getString(Shared.KEY_LONGITUDE, "0")));
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ConnectionStateMonitor.networkState != NetworkState.STATE_AVAILABLE) return;
                if (count > 1){
                    if (task != null) task.cancel(true);
                    task = new SearchBranchTask();
                    task.execute(s.toString());
                    Log.d("SearchActivity", "searching for "+s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(SearchActivity.this);
                    return false;
                }
            });
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class SearchBranchTask extends AsyncTask<String,String,String> {
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
                URL url = new URL(RequestUrl.BRANCH + "/search?name=" + strings[0]);
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
                            Toast.makeText(SearchActivity.this, "Cannot get current location, try again", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // show branch
                        recyclerView.setAdapter(new RecyclerViewBranchAdapter(SearchActivity.this, branches));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(SearchActivity.this, getResources().getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
                    break;
                case ResultsCode.FAILED:
                    Toast.makeText(SearchActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                    break;
                case ResultsCode.IO_EXCEPTION:
                    Toast.makeText(SearchActivity.this, getResources().getString(R.string.io_exception), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}