package com.luanvan.customer.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;
import com.google.android.material.button.MaterialButton;
import com.luanvan.customer.LoginActivity;
import com.luanvan.customer.ManagerProfileActivity;
import com.luanvan.customer.PaymentActivity;
import com.luanvan.customer.R;
import com.luanvan.customer.SignupActivity;
import com.luanvan.customer.components.Shared;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class MeFragment extends Fragment {
    private MaterialButton btnLogin, btnSignup;
    private TextView tvPayment, tvAddress, tvManagerProfile, tvLogout;
    private RelativeLayout layoutProgressBar;
    private LinearLayout layoutNotLogin, layoutLogin;
    private TextView tvUsername, tvName;
    private String token = "";
    private String user = "";

    String username = "";
    String firstName = "";
    String lastName = "";
    String phoneNumber = "";
    String dayOfBirth = "";
    String gender = "";
    String email = "";
    String consumerID = "";
    private ProgressBar progressBar;
    public MeFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnSignup = view.findViewById(R.id.btnSignup);
        tvPayment = view.findViewById(R.id.tvPayment);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvManagerProfile = view.findViewById(R.id.tvManagerProfile);
        tvLogout = view.findViewById(R.id.tvLogout);
        layoutProgressBar = view.findViewById(R.id.layoutProfile);
        layoutNotLogin = view.findViewById(R.id.layoutNotLogin);
        layoutLogin = view.findViewById(R.id.layoutLogin);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvName = view.findViewById(R.id.tvName);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleSmall);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250, 250);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutProgressBar.addView(progressBar, params);
        progressBar.setVisibility(View.INVISIBLE);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "")+"";
        if (!token.equals("")){
            // logged in
            String TOKEN_PREFIX = "Bearer ";
            JWT jwt = new JWT(token.replace(TOKEN_PREFIX,""));
            user = jwt.getSubject();

            new GetUserDataTask().execute(token, user);
        } else {
            layoutNotLogin.setVisibility(View.VISIBLE);
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SignupActivity.class));
            }
        });
        tvPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PaymentActivity.class));
            }
        });
        tvManagerProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ManagerProfileActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("firstName", firstName);
                intent.putExtra("lastName", lastName);
                intent.putExtra("dayOfBirth", dayOfBirth);
                intent.putExtra("gender", gender);
                intent.putExtra("email", email);
                intent.putExtra("consumerID", consumerID);
                startActivity(intent);
            }
        });
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE).edit();
                editor.putString(Shared.KEY_BEARER, "");
                editor.apply();

                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.logout_success), Toast.LENGTH_LONG).show();

                layoutLogin.setVisibility(View.INVISIBLE);
                layoutNotLogin.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_me, container, false);
    }

    private class GetUserDataTask extends AsyncTask<String, String, String> {
        private final String consumerURL = "https://orefoo.herokuapp.com/user/consumer";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(consumerURL+"?username="+params[1]);
                connection = (HttpURLConnection) url.openConnection();
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
            } catch (SocketTimeoutException e) {
                Toast.makeText(getActivity(), getResources().getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
            } catch (IOException e){
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

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressBar.setVisibility(View.INVISIBLE);
            layoutLogin.setVisibility(View.VISIBLE);

            if (result == null) return;
            try {
                JSONObject user = new JSONObject(result);
                username = user.getString("username");
                JSONObject consumer = user.getJSONObject("consumer");
                firstName = consumer.getString("firstName").equals("null") ? "empty":consumer.getString("firstName");
                lastName = consumer.getString("lastName").equals("null") ? "empty":consumer.getString("lastName");
                phoneNumber = consumer.getString("phoneNumber").equals("null") ? "empty":consumer.getString("phoneNumber");
                dayOfBirth = consumer.getString("dayOfBirth").equals("null") ? "empty":consumer.getString("dayOfBirth");
                gender = consumer.getString("gender").equals("null") ? "empty":consumer.getString("gender");
                email = consumer.getString("email").equals("null") ? "empty":consumer.getString("email");
                consumerID = consumer.getString("id");

                tvUsername.setText(username);
                tvName.setText(firstName.equals("empty")&&lastName.equals("empty") ? "(empty)":lastName+" "+firstName);

                SharedPreferences.Editor editor = getActivity().getSharedPreferences(Shared.PROFILE, Context.MODE_PRIVATE).edit();
                editor.putString("firstName", firstName);
                editor.putString("lastName", lastName);
                editor.putString("username", username);
                editor.apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        if (getActivity()==null || tvName == null) return;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Shared.PROFILE, Context.MODE_PRIVATE);
        tvName.setText(sharedPreferences.getString("lastName", "")+" "+sharedPreferences.getString("firstName", ""));
        tvUsername.setText(sharedPreferences.getString("username", ""));
        super.onResume();
    }
}