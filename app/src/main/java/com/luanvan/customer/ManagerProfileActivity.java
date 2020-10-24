package com.luanvan.customer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;
import com.google.android.material.appbar.MaterialToolbar;
import com.luanvan.customer.components.RequestsCode;

public class ManagerProfileActivity extends AppCompatActivity {

    private TextView tvEdit;
    private TextView tvUsername, tvPhoneNumber, tvName, tvEmail, tvGender, tvDoB;
    private MaterialToolbar toolbar;

    String username = "";
    String firstName = "";
    String lastName = "";
    String phoneNumber = "";
    String dayOfBirth = "";
    String gender = "";
    String email = "";
    String consumerID = "";
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_profile);

        tvEdit = findViewById(R.id.tvEdit);
        tvUsername = findViewById(R.id.tvUsername);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvGender = findViewById(R.id.tvGender);
        tvDoB = findViewById(R.id.tvDoB);
        toolbar = findViewById(R.id.toolbar);

        username = getIntent().getStringExtra("username");
        firstName = getIntent().getStringExtra("firstName");
        lastName = getIntent().getStringExtra("lastName");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        dayOfBirth = getIntent().getStringExtra("dayOfBirth");
        gender = getIntent().getStringExtra("gender");
        email = getIntent().getStringExtra("email");
        consumerID = getIntent().getStringExtra("consumerID");

        tvUsername.setText(username);
        tvPhoneNumber.setText(phoneNumber);
        tvName.setText(lastName+" "+firstName);
        tvEmail.setText(email);
        tvGender.setText(gender);
        tvDoB.setText(dayOfBirth);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerProfileActivity.this, UpdateProfileActivity.class);

                intent.putExtra("username", username);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("firstName", firstName);
                intent.putExtra("lastName", lastName);
                intent.putExtra("email", email);
                intent.putExtra("gender", gender);
                intent.putExtra("dayOfBirth", dayOfBirth);
                intent.putExtra("consumerID", consumerID);

                startActivityForResult(intent, RequestsCode.REQUEST_UPDATE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestsCode.REQUEST_UPDATE && resultCode == Activity.RESULT_OK && data != null){
            username = data.getStringExtra("username");
            phoneNumber = data.getStringExtra("phoneNumber");
            firstName = data.getStringExtra("firstName");
            lastName = data.getStringExtra("lastName");
            email = data.getStringExtra("email");
            gender = data.getStringExtra("gender");
            dayOfBirth = data.getStringExtra("dayOfBirth");

            tvUsername.setText(username);
            tvPhoneNumber.setText(phoneNumber);
            tvName.setText(lastName+" "+firstName);
            tvEmail.setText(email);
            tvGender.setText(gender);
            tvDoB.setText(dayOfBirth);
        }
    }
}