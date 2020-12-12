package com.luanvan.customer;

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
import android.widget.Toast;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.luanvan.customer.components.RequestUrl;
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

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText etOldPassword, etNewPassword, etConfirmNewPassword;
    private MaterialButton btnChange;
    private MaterialToolbar toolbar;
    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;

    private String token;
    private int userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnChange = findViewById(R.id.btnChange);
        layoutProgressBar = findViewById(R.id.layoutProgressBar);
        toolbar = findViewById(R.id.toolbar);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        layoutProgressBar.addView(progressBar, params);
        progressBar.setVisibility(View.INVISIBLE);

        SharedPreferences sharedPreferences = getSharedPreferences(Shared.TOKEN, MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "");

        userId = getUserId(token);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etNewPassword.getText().toString().equals(etConfirmNewPassword.getText().toString())){
                    Toast.makeText(ChangePasswordActivity.this, getString(R.string.password_not_match), Toast.LENGTH_SHORT).show();
                    return;
                } else if (etOldPassword.getText().toString().equals("") || etNewPassword.getText().toString().equals("") || etConfirmNewPassword.getText().toString().equals("")){
                    Toast.makeText(ChangePasswordActivity.this, getString(R.string.must_fill), Toast.LENGTH_SHORT).show();
                    return;
                }
                new ChangePasswordTask().execute(userId+"", etOldPassword.getText().toString(), etNewPassword.getText().toString());
            }
        });
    }

    public int getUserId(String token) {
        String TOKEN_PREFIX = "Bearer ";
        JWT jwt = new JWT(token.replace(TOKEN_PREFIX, ""));
        Claim claim = jwt.getClaim("userId");
        return claim.asInt();
    }

    @SuppressLint("StaticFieldLeak")
    private class ChangePasswordTask extends AsyncTask<String,String,String> {
        private OutputStream os;

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
                URL url = new URL(RequestUrl.USER + strings[0] + "/change-password");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("oldPassword", strings[1]);
                jsonObject.put("newPassword", strings[2]);
                String data = jsonObject.toString();

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PATCH");
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.setRequestProperty("Authorization", token);
                connection.setDoOutput(true);
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setFixedLengthStreamingMode(data.getBytes().length);
                connection.connect();

                os = new BufferedOutputStream(connection.getOutputStream());
                os.write(data.getBytes());
                os.flush();

                InputStream is;
                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+"");
                if (statusCode == 200) return "200";
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
                    Log.d("ResponseChangePass: ", "> " + line);
                }
                return buffer.toString();

            } catch (SocketTimeoutException e) {
                return ResultsCode.SOCKET_TIMEOUT+"";
            } catch (IOException | JSONException e){
                return "";
            } finally {
                try {
                    if (os!=null) os.close();
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
            if (s.equals("200")){
                Toast.makeText(ChangePasswordActivity.this, getResources().getString(R.string.change_password_success), Toast.LENGTH_LONG).show();

                finish();
            } else if (s.equals(ResultsCode.SOCKET_TIMEOUT+"")){
                Toast.makeText(ChangePasswordActivity.this, getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONObject apierror = jsonObject.getJSONObject("apierror");
                    if (apierror.getString("message").contains("Old password incorrect")){
                        Toast.makeText(ChangePasswordActivity.this, getString(R.string.old_password_incorrect), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, getString(R.string.change_password_failed), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ChangePasswordActivity.this, getString(R.string.change_password_failed)+" error: "+e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}