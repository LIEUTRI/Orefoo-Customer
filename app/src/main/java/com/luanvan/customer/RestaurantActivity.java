package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.luanvan.customer.Fragments.CommentFragment;
import com.luanvan.customer.Fragments.MenuFragment;
import com.luanvan.customer.Fragments.RestaurantInfoFragment;
import com.luanvan.customer.components.AddItemDialog;
import com.luanvan.customer.components.CalculateDistanceTime;
import com.luanvan.customer.components.Shared;
import com.luanvan.customer.components.UserData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Hashtable;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class RestaurantActivity extends AppCompatActivity {

    private TextView tvMenu;
    private TextView tvName, tvAddress;
    private TextView tvDistance, tvTime;
    private AlertDialog dialogDistance;
    private ImageView ivBranchBackground;
    private ImageView ivQRBranch;
    private MaterialToolbar toolbar;

    private String name;
    private String phone;
    private String imgURL;
    private String openTime, closeTime;
    private String address;
    private Boolean isSell;
    private int id;
    private double latitude, longitude;
    public static boolean tooFar = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        tvMenu = findViewById(R.id.tvMenu);
        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvDistance = findViewById(R.id.tvDistance);
        tvTime = findViewById(R.id.tvTime);
        ivBranchBackground = findViewById(R.id.ivBranchBackground);
        ivQRBranch = findViewById(R.id.ivQRBranch);
        toolbar = findViewById(R.id.toolbar);

        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        imgURL = getIntent().getStringExtra("imgURL");
        openTime = getIntent().getStringExtra("openTime");
        closeTime = getIntent().getStringExtra("closeTime");
        address = getIntent().getStringExtra("address");
        isSell = getIntent().getBooleanExtra("isSell", true);
        id = getIntent().getIntExtra("id", -1);
        latitude = getIntent().getDoubleExtra("lat", 0);
        longitude = getIntent().getDoubleExtra("lng", 0);

        // data for QR code
        JSONObject jsonQR = new JSONObject();
        try {
            jsonQR.put("task", "showbranch");
            JSONObject jsonData = new JSONObject();
            jsonData.put("id", id);
            jsonData.put("name", name);
            jsonData.put("phone", phone);
            jsonData.put("imageURL", imgURL);
            jsonData.put("openTime", openTime);
            jsonData.put("closeTime", closeTime);
            jsonData.put("address", address);
            jsonData.put("isSell", true);
            jsonData.put("lat", latitude);
            jsonData.put("lng", longitude);
            jsonQR.put("data", jsonData);

            ivQRBranch.setImageBitmap(generateQRCodeBitmap(jsonQR.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////



        ////////////////////////////////////////////////////////////////////////////////////////////
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.bg_chagio)
                .error(R.drawable.bg_chagio);
        Glide.with(this).load(imgURL).apply(options).into(ivBranchBackground);

        ////////////////////////////////////////////////////////////////////////////////////////////
        SharedPreferences.Editor editor = getSharedPreferences(Shared.BRANCH, Context.MODE_PRIVATE).edit();
        editor.putString(Shared.KEY_BRANCH_NAME, name);
        editor.putString(Shared.KEY_LATITUDE, latitude+"");
        editor.putString(Shared.KEY_LONGITUDE, longitude+"");
        editor.apply();

        // default fragment
        Fragment fragment = new MenuFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_restaurant_container, fragment);
        transaction.commit();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new MenuFragment();
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_restaurant_container, fragment);
                transaction.commit();
            }
        });

        ivQRBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageDialog();
            }
        });

        // consumer location
        SharedPreferences sharedPreferences = getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE);
        LatLng consumerLatLng = new LatLng(Double.parseDouble(sharedPreferences.getString(Shared.KEY_LATITUDE, "0")),
                Double.parseDouble(sharedPreferences.getString(Shared.KEY_LONGITUDE, "0")));

        Log.i("RestaurantActivity", "location: "+consumerLatLng.latitude+"| "+latitude);

        CalculateDistanceTime distance_task = new CalculateDistanceTime(this);
        distance_task.getDirectionsUrl(consumerLatLng, new LatLng(latitude,longitude));
        distance_task.setLoadListener(new CalculateDistanceTime.taskCompleteListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void taskCompleted(String[] distance) {
                tvDistance.setText("unknown");
                tvTime.setText("unknown");
                if (distance.length > 0){
                    tvDistance.setText(distance[0]);
                    tvTime.setText(distance[1]);

                    double km = Double.parseDouble(distance[0].substring(0,distance[0].indexOf(" ")));
                    if (km <= 10.0){
                        tooFar = false;
                        SharedPreferences.Editor editor = getSharedPreferences(Shared.BRANCH, Context.MODE_PRIVATE).edit();
                        editor.putString(Shared.KEY_BRANCH_DISTANCE, km+"");
                        editor.apply();
                    } else {
                        tooFar = true;
                        showDialogDistance(RestaurantActivity.this);
                    }
                }
            }
        });

        tvName.setText(name);
        tvAddress.setText(address);
    }

    private void showImageDialog(){
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });

        ImageView image = new ImageView(this);
        image.setImageDrawable(ivQRBranch.getDrawable());

        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        dialog.addContentView(image, new RelativeLayout.LayoutParams(width, width));
        dialog.show();
    }

    private Bitmap generateQRCodeBitmap(String data){
        Hashtable<EncodeHintType, String> hashtable = new Hashtable<>();
        hashtable.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, (int) DPtoPX(120), (int) DPtoPX(120), hashtable);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x=0; x<width; x++){
                for (int y=0; y<height; y++){
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK:Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e){
            e.printStackTrace();
        }
        return null;
    }

    public float DPtoPX(float dip){
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                getResources().getDisplayMetrics()
        );
    }

    private void showDialogDistance(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.too_far));
        builder.setMessage(context.getResources().getString(R.string.too_far_message));
        builder.setCancelable(false);
        builder.setPositiveButton(
                context.getResources().getString(R.string.change_branch),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }
        );
        builder.setNegativeButton(
                context.getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }
        );

        dialogDistance = builder.create();
        dialogDistance.show();
    }
}