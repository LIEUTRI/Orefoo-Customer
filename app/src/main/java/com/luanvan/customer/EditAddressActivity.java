package com.luanvan.customer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.MaterialToolbar;
import com.luanvan.customer.components.RequestsCode;
import com.luanvan.customer.components.ResultsCode;
import com.luanvan.customer.components.Shared;

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

public class EditAddressActivity extends AppCompatActivity {
    private TextView tvSave;
    private TextView tvAddress;
    private EditText etAddressDetail;
    private EditText etContactName, etContactPhone;
    private MaterialToolbar toolbar;

    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;

    private double consumerLat,consumerLng;
    private String consumerAddress;
    private int consumerID;
    private String consumerName, consumerPhone;
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);

        tvSave = findViewById(R.id.tvSave);
        tvAddress = findViewById(R.id.tvAddress);
        etAddressDetail = findViewById(R.id.etAddressDetail1);
        etContactName = findViewById(R.id.etContactName);
        etContactPhone= findViewById(R.id.etContactPhone);
        layoutProgressBar = findViewById(R.id.layoutProgressBar);
        toolbar = findViewById(R.id.toolbar);

        SharedPreferences sharedPreferences = getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "");

        sharedPreferences = getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE);

        consumerID = sharedPreferences.getInt(Shared.KEY_CONSUMER_ID, -1);
        consumerAddress = sharedPreferences.getString(Shared.KEY_ADDRESS, "");
        consumerLat = Double.parseDouble(sharedPreferences.getString(Shared.KEY_LATITUDE, "0"));
        consumerLng = Double.parseDouble(sharedPreferences.getString(Shared.KEY_LONGITUDE, "0"));
        consumerName = sharedPreferences.getString(Shared.KEY_LAST_NAME, "") +" "+ sharedPreferences.getString(Shared.KEY_FIRST_NAME, "");
        consumerPhone = sharedPreferences.getString(Shared.KEY_PHONE, "");

        tvAddress.setText(consumerAddress);
        etContactName.setText(consumerName);
        etContactPhone.setText(consumerPhone);

        ////////////////////////////////////////////////////////////////////////////////////////////
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        layoutProgressBar.addView(progressBar, params);
        progressBar.setVisibility(View.INVISIBLE);
        ////////////////////////////////////////////////////////////////////////////////////////////

        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditAddressActivity.this, PickLocationActivity.class);
                startActivityForResult(intent, RequestsCode.REQUEST_ADDRESS);
            }
        });

        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                saveInfo();
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void saveInfo(){
        String detail = etAddressDetail.getText().toString();
        if (!detail.equals("")){
            consumerAddress = detail+", "+consumerAddress;
        }
        consumerName = etContactName.getText().toString();
        consumerPhone = etContactPhone.getText().toString();

        new UpdateLocationTask(consumerLat, consumerLng, consumerAddress, consumerID).execute();

        SharedPreferences.Editor editor = getSharedPreferences(Shared.CONSUMER, MODE_PRIVATE).edit();
        editor.putString(Shared.KEY_LATITUDE, consumerLat+"");
        editor.putString(Shared.KEY_LONGITUDE, consumerLng+"");
        editor.putString(Shared.KEY_ADDRESS, consumerAddress);
        editor.putString(Shared.KEY_FIRST_NAME, consumerName);
        editor.putString(Shared.KEY_LAST_NAME, "");
        editor.putString(Shared.KEY_PHONE, consumerPhone);
        editor.apply();
    }

    @SuppressLint("StaticFieldLeak")
    class UpdateLocationTask extends AsyncTask<String,String,String> {
        private OutputStream os;
        private InputStream is;
        private double latitude, longitude;
        private String address;
        private int consumerID;
        private int resultCode;
        private final String locationURL = "https://orefoo.herokuapp.com/consumer-location?consumer-id=";

        public UpdateLocationTask(double latitude, double longitude, String address, int consumerID){
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.consumerID = consumerID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(locationURL+consumerID);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("latitude", latitude);
                jsonObject.put("longitude", longitude);
                jsonObject.put("address", address);
                String data = jsonObject.toString();

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PATCH");
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Authorization", token);
                connection.setDoOutput(true);
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setFixedLengthStreamingMode(data.getBytes().length);
                connection.connect();

                os = new BufferedOutputStream(connection.getOutputStream());
                os.write(data.getBytes());
                os.flush();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+"");
                if (statusCode >= 200 && statusCode < 400){
                    resultCode = ResultsCode.SUCCESS;
                    is = connection.getInputStream();
                } else {
                    resultCode = ResultsCode.FAILED;
                    is = connection.getErrorStream();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                    Log.d("ResponseUpdateLocation", "> " + line);
                }
                return buffer.toString();

            } catch (SocketTimeoutException e) {
                resultCode = ResultsCode.SOCKET_TIMEOUT;
            } catch (IOException | JSONException e){
                e.printStackTrace();
            } finally {
                try {
                    if (os!=null) os.close();
                    if (is!=null) is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (connection != null) connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressBar.setVisibility(View.INVISIBLE);

            if (s==null) return;

            switch (resultCode){
                case ResultsCode.SUCCESS:
                    Toast.makeText(EditAddressActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra(Shared.KEY_FIRST_NAME, consumerName);
                    intent.putExtra(Shared.KEY_PHONE, consumerPhone);
                    intent.putExtra(Shared.KEY_ADDRESS, consumerAddress);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    break;
                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(EditAddressActivity.this, getResources().getString(R.string.socket_timeout), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(EditAddressActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestsCode.REQUEST_ADDRESS && resultCode == Activity.RESULT_OK && data != null){
            consumerAddress = data.getStringExtra(Shared.KEY_ADDRESS);
            tvAddress.setText(consumerAddress);
            consumerLat = data.getDoubleExtra(Shared.KEY_LATITUDE, 0);
            consumerLng = data.getDoubleExtra(Shared.KEY_LONGITUDE, 0);
        }
    }
}