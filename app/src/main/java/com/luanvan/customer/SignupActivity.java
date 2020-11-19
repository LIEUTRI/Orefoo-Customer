package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.luanvan.customer.components.RequestUrl;

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

public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvBackToLogin;
    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btn_signup);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        layoutProgressBar = findViewById(R.id.layoutProgressBar);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignupTask signupTask = new SignupTask();
                signupTask.execute(etUsername.getText().toString(), etPassword.getText().toString());
            }
        });
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250, 250);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutProgressBar.addView(progressBar, params);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private class SignupTask extends AsyncTask<String,String,String> {

        OutputStream os;
        InputStream is;
        BufferedReader reader = null;
        JSONObject json = new JSONObject();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            //http post
            try {
                URL url = new URL( RequestUrl.SIGNUP );

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", strings[0]);
                jsonObject.put("password", strings[1]);
                String data = jsonObject.toString();

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
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

                reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    Log.d("Response: ", ">>> " + line);
                }
                json = new JSONObject(buffer.toString());
                json.put("statusCode", statusCode);
                return json.toString();

            } catch (SocketTimeoutException e) {
                try {
                    json.put("statusCode", 0);
                    json.put("message", getResources().getString(R.string.socket_timeout));
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
                return json.toString();
            } catch (IOException e){
                try {
                    json.put("statusCode", 0);
                    json.put("message", getResources().getString(R.string.check_internet_connection));
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
                return json.toString();
            } catch (Exception e){
                try {
                    json.put("statusCode", 0);
                    json.put("message", getResources().getString(R.string.signup_failed));
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
                return json.toString();
            }finally {
                try {
                    if (os != null && is != null) {
                        os.close();
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (connection != null) connection.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressBar.setVisibility(View.INVISIBLE);

            if (s == null) return;

            try {
                JSONObject jsonObject = new JSONObject(s);
                int statusCode = jsonObject.getInt("statusCode");
                Log.i("statusCode", statusCode+"");
                if (statusCode == 200){
                    Toast.makeText(SignupActivity.this, getResources().getString(R.string.signup_success), Toast.LENGTH_LONG).show();
                } else if (statusCode == 409 && jsonObject.toString().contains("CONFLICT")){
                    Toast.makeText(SignupActivity.this, getResources().getString(R.string.account_exist), Toast.LENGTH_LONG).show();
                } else if (statusCode == 0){
                    Toast.makeText(SignupActivity.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SignupActivity.this, getResources().getString(R.string.signup_failed), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}