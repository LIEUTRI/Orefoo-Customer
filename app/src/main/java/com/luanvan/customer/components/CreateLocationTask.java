package com.luanvan.customer.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;
import com.luanvan.customer.LoginActivity;
import com.luanvan.customer.R;

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

public class CreateLocationTask extends AsyncTask<String,String,String> {
    private Context context;
    private OutputStream os;
    private InputStream is;
    private String token;
    private String user;
    private double latitude, longitude;
    private String address;
    private String consumerID;
    private final String locationURL = "https://orefoo.herokuapp.com/consumer-location";
    private final String PREF_TOKEN = "PREF_TOKEN";
    private final String KEY_BEARER = "bearer";

    public CreateLocationTask(Context context, double latitude, double longitude, String address, String consumerID){
        this.context = context;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.consumerID = consumerID;
    }

    @Override
    protected String doInBackground(String... strings) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(KEY_BEARER, "");
        if (!token.equals("")){
            // logged in
            String TOKEN_PREFIX = "Bearer ";
            JWT jwt = new JWT(token.replace(TOKEN_PREFIX,""));
            user = jwt.getSubject();
        } else {
            return null;
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL(locationURL);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
            jsonObject.put("address", address);
            JSONObject jsonConsumer = new JSONObject();
            jsonConsumer.put("id", consumerID);
            jsonObject.put("consumer", jsonConsumer);
            String data = jsonObject.toString();

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
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
                is = connection.getInputStream();
            } else {
                is = connection.getErrorStream();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder buffer = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null){
                buffer.append(line).append("\n");
                Log.d("Response: ", "> " + line);
            }
            return buffer.toString();

        } catch (SocketTimeoutException e) {
            Toast.makeText(context, context.getResources().getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
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
        if (s==null) return;
    }
}
