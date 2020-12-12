package com.luanvan.customer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import com.google.android.material.appbar.MaterialToolbar;
import com.luanvan.customer.components.RequestsCode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ManagerProfileActivity extends AppCompatActivity {

    private TextView tvEdit;
    private TextView tvUsername, tvPhoneNumber, tvName, tvEmail, tvGender, tvDoB;
    private TextView tvPassword;
    private MaterialToolbar toolbar;

    private String username = "";
    private String firstName = "";
    private String lastName = "";
    private String phoneNumber = "";
    private String dayOfBirth = "";
    private String gender = "";
    private String email = "";
    private int consumerID;
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
        tvPassword = findViewById(R.id.tvPassword);
        toolbar = findViewById(R.id.toolbar);

        username = getIntent().getStringExtra("username");
        firstName = getIntent().getStringExtra("firstName");
        lastName = getIntent().getStringExtra("lastName");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        dayOfBirth = getIntent().getStringExtra("dayOfBirth");
        gender = getIntent().getStringExtra("gender");
        email = getIntent().getStringExtra("email");
        consumerID = getIntent().getIntExtra("consumerID", -1);

        tvUsername.setText(username);
        tvPhoneNumber.setText(phoneNumber);
        tvName.setText(lastName+" "+firstName);
        tvEmail.setText(email);
        tvGender.setText(gender);
        updateDate(dayOfBirth);

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

        tvPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManagerProfileActivity.this, ChangePasswordActivity.class));
            }
        });
    }

    private void updateDate(String date){
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(date));
            tvDoB.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(calendar.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            updateDate(dayOfBirth);
        }
    }
}