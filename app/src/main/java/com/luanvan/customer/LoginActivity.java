package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.material.appbar.MaterialToolbar;
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

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private EditText etUsername, etPassword;
    private LoginButton btnFBLogin;
    private MaterialToolbar toolbar;
    private TextView tvGotoSignup;
    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;

    CallbackManager callbackManager;
    String token;

    String username;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btn_login);
        toolbar = findViewById(R.id.toolbarLogin);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvGotoSignup = findViewById(R.id.tvGotoSignup);
        layoutProgressBar = findViewById(R.id.layoutProgressBar);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250, 250);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutProgressBar.addView(progressBar, params);
        progressBar.setVisibility(View.INVISIBLE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = etUsername.getText().toString();
                password = etPassword.getText().toString();
                LoginTask loginTask = new LoginTask();
                loginTask.execute();
            }
        });

        tvGotoSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
        /////////////////////////////////////////////////////

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class LoginTask extends AsyncTask<String,String,String> {

        private OutputStream os;
        private InputStream is;
        JSONObject json = new JSONObject();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            //http post
            try {
                URL url = new URL( RequestUrl.LOGIN );

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("password", password);
                jsonObject.put("role", new JSONObject().put("name", "consumer"));
                String data = jsonObject.toString();

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.setRequestProperty("Authorization", "application/json;charset=utf-8");
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

                json = new JSONObject();
                json.put("status", statusCode);

                if (statusCode == HttpURLConnection.HTTP_OK){
                    json.put("token", connection.getHeaderField("Authorization"));
                }
                return json.toString();
            } catch (SocketTimeoutException e) {
                try {
                    json.put("status", ResultsCode.SOCKET_TIMEOUT);
                    return json.toString();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            } catch (IOException | JSONException e){
                try {
                    json.put("status", ResultsCode.FAILED);
                    return json.toString();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
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

            try {
                JSONObject jsonObject = new JSONObject(s);
                int statusCode = jsonObject.getInt("status");
                if (statusCode == 200){
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_success), Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor editor = getSharedPreferences(Shared.TOKEN, MODE_PRIVATE).edit();
                    editor.putString(Shared.KEY_BEARER, jsonObject.getString("token"));
                    editor.apply();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else if (statusCode == 422){
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.incorrect_username_password), Toast.LENGTH_LONG).show();
                } else if(statusCode == 0){
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class GetTokenTask extends AsyncTask<String,String,String> {

        BufferedReader reader = null;
        OutputStream os;
        InputStream is;
        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            HttpURLConnection connection = null;
            //http post
            try {
                URL url = new URL(RequestUrl.LOGIN);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("authToken", token);
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
                return buffer.toString();

            } catch (Exception e) {
                Log.e("log_tag", "Error converting result " + e.toString());
            } finally {
//                try {
//                    os.close();
//                    is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s != null){
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String bearer = jsonObject.getString("token");
                    new GetData().execute(bearer);
                }catch (JSONException err){
                    Log.d("Error", err.toString());
                }
            }
        }
    }

    private class GetData extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(RequestUrl.LOGIN);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.setRequestProperty("Authorization", params[0]);
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    Log.d("Response: ", "> " + line);

                }
                return buffer.toString();
            } catch (IOException e) {
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
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject user = new JSONObject(result);
                Toast.makeText(LoginActivity.this, "Hello "+user.getString("firstname"), Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}