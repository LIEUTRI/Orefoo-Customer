package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvBackToLogin;

    public final String signupURL = "https://orefoo.herokuapp.com/user/consumer";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btn_signup);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignupTask signupTask = new SignupTask();
                signupTask.execute(etUsername.getText().toString(), etPassword.getText().toString());
            }
        });
    }

    private class SignupTask extends AsyncTask<String,String,String> {

        OutputStream os;
        InputStream is;
        BufferedReader reader = null;
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            //http post
            try {
                URL url = new URL(signupURL);

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
                if (statusCode == 200){
                    JSONObject json = new JSONObject(buffer.toString());
                    json.put("status", 200);
                    return json.toString();
                }
                return buffer.toString();

            } catch (Exception e) {
                Log.e("log_tag", "Error converting result " + e.toString());
            } finally {
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

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s == null) return;

            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("status").equals("200")){
                    Toast.makeText(SignupActivity.this, getResources().getString(R.string.signup_success), Toast.LENGTH_LONG).show();
                } else if (jsonObject.getString("status").equals("500")){
                    Toast.makeText(SignupActivity.this, getResources().getString(R.string.account_exist), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SignupActivity.this, getResources().getString(R.string.signup_failed), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}