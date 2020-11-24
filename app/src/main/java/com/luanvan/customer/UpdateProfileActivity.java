package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.luanvan.customer.components.RequestUrl;
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
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText etUsername, etPhoneNumber, etFirstName, etLastName, etEmail, etGender, etDOB;
    private MaterialButton btnSave;
    private MaterialToolbar toolbar;
    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;

    private Calendar calendar = null;
    private DatePickerDialog.OnDateSetListener onDateSetListener = null;

    private String username = "";
    private String firstName = "";
    private String lastName = "";
    private String phoneNumber = "";
    private String dayOfBirth = "";
    private String gender = "";
    private String email = "";
    private int consumerID = -1;

    public final String TAG = "UpdateProfileActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        etUsername = findViewById(R.id.etUsername);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etGender = findViewById(R.id.etGender);
        etDOB = findViewById(R.id.etDOB);
        btnSave = findViewById(R.id.btnSave);
        toolbar = findViewById(R.id.toolbar);
        layoutProgressBar = findViewById(R.id.layoutProgressBar);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        layoutProgressBar.addView(progressBar, params);
        progressBar.setVisibility(View.INVISIBLE);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        username = getIntent().getStringExtra("username");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        firstName = getIntent().getStringExtra("firstName");
        lastName = getIntent().getStringExtra("lastName");
        email = getIntent().getStringExtra("email");
        gender = getIntent().getStringExtra("gender");
        dayOfBirth = getIntent().getStringExtra("dayOfBirth");

        /////////////////////////////////////////////////////////////////////////
        calendar = Calendar.getInstance();

        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        try {
            calendar.setTime(simpleDateFormat.parse(dayOfBirth));
            updateUIDate(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateUIDate(calendar.getTime());

                dayOfBirth = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(calendar.getTime());
            }
        };
        /////////////////////////////////////////////////////////////////////////

        etUsername.setText(username);
        etPhoneNumber.setText(phoneNumber.equals("empty") ? "":phoneNumber);
        etFirstName.setText(firstName.equals("empty") ? "":firstName);
        etLastName.setText(lastName.equals("empty") ? "":lastName);
        etEmail.setText(email.equals("empty") ? "":email);
        etGender.setText(gender.equals("empty") ? "":gender);

        SharedPreferences sharedPreferences = getSharedPreferences(Shared.TOKEN, MODE_PRIVATE);
        final String token = sharedPreferences.getString(Shared.KEY_BEARER, "");

        sharedPreferences = getSharedPreferences(Shared.CONSUMER, MODE_PRIVATE);
        consumerID = sharedPreferences.getInt(Shared.KEY_CONSUMER_ID, -1);

        etDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(UpdateProfileActivity.this, onDateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                phoneNumber = etPhoneNumber.getText().toString();
                firstName = etFirstName.getText().toString();
                lastName = etLastName.getText().toString();
                gender = etGender.getText().toString();
                email = etEmail.getText().toString();

                UpdateProfileTask task = new UpdateProfileTask();
                task.execute(consumerID+"", firstName, lastName, dayOfBirth, gender, phoneNumber, email, token);
            }
        });
    }

    public int getUserId(String token){
        String TOKEN_PREFIX = "Bearer ";
        JWT jwt = new JWT(token.replace(TOKEN_PREFIX,""));
        Claim claim = jwt.getClaim("userId");
        return claim.asInt();
    }

    private void updateUIDate(Date date) {
        String dateFormat = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        etDOB.setText(simpleDateFormat.format(date));
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateProfileTask extends AsyncTask<String,String,String> {
        private OutputStream os;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            Log.i("token", strings[7]);
            //http post
            try {
                URL url = new URL(RequestUrl.CONSUMER + strings[0]);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("firstName", strings[1]);
                jsonObject.put("lastName", strings[2]);
                jsonObject.put("dayOfBirth", strings[3]);
                jsonObject.put("gender", strings[4]);
                jsonObject.put("phoneNumber", strings[5]);
                jsonObject.put("email", strings[6]);
                String data = jsonObject.toString();
                Log.i("json request", data);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PATCH");
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.setRequestProperty("Authorization", strings[7]);
                connection.setDoOutput(true);
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setFixedLengthStreamingMode(data.getBytes().length);
                connection.connect();

                os = new BufferedOutputStream(connection.getOutputStream());
                os.write(data.getBytes());
                os.flush();

                InputStream is = null;
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
                    Log.d("ResponseUpdateProfile: ", "> " + line);
                }
                return buffer.toString();

            } catch (SocketTimeoutException e) {
                return ResultsCode.SOCKET_TIMEOUT+"";
            } catch (IOException | JSONException e){
                e.printStackTrace();
            } finally {
                try {
                    if (os!=null) os.close();
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

            if (s == null) return;
            if (s.equals("200")){
                Toast.makeText(UpdateProfileActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_LONG).show();

                Intent intent = new Intent();
                intent.putExtra("username", username);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("firstName", firstName);
                intent.putExtra("lastName", lastName);
                intent.putExtra("email", email);
                intent.putExtra("gender", gender);
                intent.putExtra("dayOfBirth", dayOfBirth);
                setResult(Activity.RESULT_OK, intent);

                SharedPreferences.Editor editor = getSharedPreferences(Shared.PROFILE, Context.MODE_PRIVATE).edit();
                editor.putString("firstName", firstName);
                editor.putString("lastName", lastName);
                editor.putString("username", username);
                editor.apply();
            } else if (s.equals(ResultsCode.SOCKET_TIMEOUT+"")){
                Toast.makeText(UpdateProfileActivity.this, getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONObject apierror = jsonObject.getJSONObject("apierror");
                    if (apierror.getString("status").equals("CONFLICT")){
                        if (apierror.getString("debugMessage").contains("phone_number")){
                            Toast.makeText(UpdateProfileActivity.this, getString(R.string.conflict_phone), Toast.LENGTH_LONG).show();
                        } else if (apierror.getString("debugMessage").contains("email")){
                            Toast.makeText(UpdateProfileActivity.this, getString(R.string.conflict_email), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(UpdateProfileActivity.this, getString(R.string.update_failed), Toast.LENGTH_LONG).show();
                    }
                    setResult(Activity.RESULT_CANCELED);
                } catch (Exception e) {
                    Toast.makeText(UpdateProfileActivity.this, getString(R.string.update_failed)+" error: "+e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}