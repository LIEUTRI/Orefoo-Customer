package com.luanvan.customer.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.luanvan.customer.BranchActivity;
import com.luanvan.customer.CategoryActivity;
import com.luanvan.customer.LoginActivity;
import com.luanvan.customer.PickLocationActivity;
import com.luanvan.customer.QRScannerActivity;
import com.luanvan.customer.R;
import com.luanvan.customer.RestaurantActivity;
import com.luanvan.customer.SearchActivity;
import com.luanvan.customer.components.Branch;
import com.luanvan.customer.components.CartDialog;
import com.luanvan.customer.components.Debug;
import com.luanvan.customer.components.RequestUrl;
import com.luanvan.customer.components.RequestsCode;
import com.luanvan.customer.components.ResultsCode;
import com.luanvan.customer.components.Shared;
import com.luanvan.customer.components.SortPlaces;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    private TextView tvSearch;
    private TextView tvAddress;
    private FloatingActionButton btnCart;
    private TextView tvSizeOfCart;
    private TextView tvSeeAllSuggest, tvSeeAllCategories;
    private TextView tvMainMeal, tvSnacks, tvDrinks, tvTraSua, tvBurgers;

    private MaterialCardView cvItem1, cvItem2, cvItem3, cvItem4, cvItem5;
    private TextView tvItemName1, tvItemName2, tvItemName3, tvItemName4, tvItemName5;
    private TextView tvKm1, tvKm2, tvKm3, tvKm4, tvKm5;
    private ImageView ivItem1, ivItem2, ivItem3, ivItem4, ivItem5;
    private TextView tvIsSell1, tvIsSell2, tvIsSell3, tvIsSell4, tvIsSell5;

    private ImageButton ibQRScan;

    private SliderLayout sliderLayout;

    private int consumerID;
    private int userId;
    private String username;
    private String consumerLocation = "";
    private String token = "";
    private int cartId;
    private ArrayList<Branch> branches = new ArrayList<>();

    private String addressLine;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location mLocation;

    private LocationManager mLocationManager;

    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;

    public final String TAG = "HomeFragment";
    public HomeFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvSearch = view.findViewById(R.id.tvSearch);
        tvAddress = view.findViewById(R.id.tvAddress);
        ivItem1 = view.findViewById(R.id.ivItem1);
        ivItem2 = view.findViewById(R.id.ivItem2);
        ivItem3 = view.findViewById(R.id.ivItem3);
        ivItem4 = view.findViewById(R.id.ivItem4);
        ivItem5 = view.findViewById(R.id.ivItem5);
        tvItemName1 = view.findViewById(R.id.tvItemName1);
        tvItemName2 = view.findViewById(R.id.tvItemName2);
        tvItemName3 = view.findViewById(R.id.tvItemName3);
        tvItemName4 = view.findViewById(R.id.tvItemName4);
        tvItemName5 = view.findViewById(R.id.tvItemName5);
        tvKm1 = view.findViewById(R.id.tvItemKm1);
        tvKm2 = view.findViewById(R.id.tvItemKm2);
        tvKm3 = view.findViewById(R.id.tvItemKm3);
        tvKm4 = view.findViewById(R.id.tvItemKm4);
        tvKm5 = view.findViewById(R.id.tvItemKm5);
        cvItem1 = view.findViewById(R.id.cardViewItem1);
        cvItem2 = view.findViewById(R.id.cardViewItem2);
        cvItem3 = view.findViewById(R.id.cardViewItem3);
        cvItem4 = view.findViewById(R.id.cardViewItem4);
        cvItem5 = view.findViewById(R.id.cardViewItem5);
        tvIsSell1 = view.findViewById(R.id.tvIsSell1);
        tvIsSell2 = view.findViewById(R.id.tvIsSell2);
        tvIsSell3 = view.findViewById(R.id.tvIsSell3);
        tvIsSell4 = view.findViewById(R.id.tvIsSell4);
        tvIsSell5 = view.findViewById(R.id.tvIsSell5);
        layoutProgressBar = view.findViewById(R.id.layoutProgressBar);
        btnCart = view.findViewById(R.id.btnCart);
        tvSizeOfCart = view.findViewById(R.id.tvSizeOfCart);
        tvSeeAllSuggest = view.findViewById(R.id.tvSeeAllSuggest);
        tvSeeAllCategories = view.findViewById(R.id.tvSeeAllCategories);
        ibQRScan = view.findViewById(R.id.ibQRScan);
        tvMainMeal = view.findViewById(R.id.tvMainMeal);
        tvSnacks = view.findViewById(R.id.tvSnacks);
        tvDrinks = view.findViewById(R.id.tvDrinks);
        tvTraSua = view.findViewById(R.id.tvTraSua);
        tvBurgers = view.findViewById(R.id.tvBurgers);
        sliderLayout = view.findViewById(R.id.slider);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Shared.CART, Context.MODE_PRIVATE);
        cartId = sharedPreferences.getInt(Shared.KEY_CART_ID, -1);

        sharedPreferences = getActivity().getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "");
        Log.i(TAG, token);

        ///////////////////////////////

        HashMap<String,String> url_maps = new HashMap<>();
        url_maps.put("Ăn sáng", "https://firebasestorage.googleapis.com/v0/b/shipper-lieutri.appspot.com/o/images%2Fslide_1.jpg?alt=media&token=5270107a-c241-4525-a757-4e141aaebc3f");
        url_maps.put("Ăn vặt", "https://firebasestorage.googleapis.com/v0/b/shipper-lieutri.appspot.com/o/images%2Fslide_2.jpg?alt=media&token=3dc83a87-60a1-4bdb-bdab-09cc5d92befc");
        url_maps.put("Trà sữa", "https://firebasestorage.googleapis.com/v0/b/shipper-lieutri.appspot.com/o/images%2Fslide_3.jpg?alt=media&token=57a5ff4f-f544-47c1-a297-6511442c34ad");

        for(String name : url_maps.keySet()){
            TextSliderView textSliderView = new TextSliderView(getActivity());
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(url_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra",name);

            sliderLayout.addSlider(textSliderView);
        }
        sliderLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
        sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        sliderLayout.setCustomAnimation(new DescriptionAnimation());
        sliderLayout.setDuration(4000);
        sliderLayout.addOnPageChangeListener(this);
        ///////////////////////////////

        if (token.contains("Bearer")) {
            username = getUsername(token);
            userId = getUserId(token);
        } else {
            getActivity().finish();
            startActivity(new Intent(getActivity(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
            return;
        }

        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, RequestsCode.REQUEST_LOCATION);
        }

        createLocationRequest();

        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SearchActivity.class));
            }
        });
        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), PickLocationActivity.class), RequestsCode.REQUEST_ADDRESS);
            }
        });
        tvSeeAllSuggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BranchActivity.class));
            }
        });
        tvSeeAllCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CategoryActivity.class));
            }
        });

        cvItem1.setEnabled(false);
        cvItem2.setEnabled(false);
        cvItem3.setEnabled(false);
        cvItem4.setEnabled(false);
        cvItem5.setEnabled(false);

        cvItem1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                intent.putExtra("id", branches.get(0).getId());
                intent.putExtra("name", branches.get(0).getName());
                intent.putExtra("phone", branches.get(0).getPhoneNumber());
                intent.putExtra("address", branches.get(0).getAddress());
                intent.putExtra("imgURL", branches.get(0).getImageUrl());
                intent.putExtra("openTime", branches.get(0).getOpeningTime());
                intent.putExtra("closeTime", branches.get(0).getClosingTime());
                intent.putExtra("isSell", branches.get(0).isSell());
                intent.putExtra("lat", branches.get(0).getLatLng().latitude);
                intent.putExtra("lng", branches.get(0).getLatLng().longitude);
                startActivity(intent);
            }
        });
        cvItem2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                intent.putExtra("id", branches.get(1).getId());
                intent.putExtra("name", branches.get(1).getName());
                intent.putExtra("phone", branches.get(1).getPhoneNumber());
                intent.putExtra("address", branches.get(1).getAddress());
                intent.putExtra("imgURL", branches.get(1).getImageUrl());
                intent.putExtra("openTime", branches.get(1).getOpeningTime());
                intent.putExtra("closeTime", branches.get(1).getClosingTime());
                intent.putExtra("isSell", branches.get(1).isSell());
                intent.putExtra("lat", branches.get(1).getLatLng().latitude);
                intent.putExtra("lng", branches.get(1).getLatLng().longitude);
                startActivity(intent);
            }
        });
        cvItem3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                intent.putExtra("id", branches.get(2).getId());
                intent.putExtra("name", branches.get(2).getName());
                intent.putExtra("phone", branches.get(2).getPhoneNumber());
                intent.putExtra("address", branches.get(2).getAddress());
                intent.putExtra("imgURL", branches.get(2).getImageUrl());
                intent.putExtra("openTime", branches.get(2).getOpeningTime());
                intent.putExtra("closeTime", branches.get(2).getClosingTime());
                intent.putExtra("isSell", branches.get(2).isSell());
                intent.putExtra("lat", branches.get(2).getLatLng().latitude);
                intent.putExtra("lng", branches.get(2).getLatLng().longitude);
                startActivity(intent);
            }
        });
        cvItem4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                intent.putExtra("id", branches.get(3).getId());
                intent.putExtra("name", branches.get(3).getName());
                intent.putExtra("phone", branches.get(3).getPhoneNumber());
                intent.putExtra("address", branches.get(3).getAddress());
                intent.putExtra("imgURL", branches.get(3).getImageUrl());
                intent.putExtra("openTime", branches.get(3).getOpeningTime());
                intent.putExtra("closeTime", branches.get(3).getClosingTime());
                intent.putExtra("isSell", branches.get(3).isSell());
                intent.putExtra("lat", branches.get(3).getLatLng().latitude);
                intent.putExtra("lng", branches.get(3).getLatLng().longitude);
                startActivity(intent);
            }
        });
        cvItem5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                intent.putExtra("id", branches.get(4).getId());
                intent.putExtra("name", branches.get(4).getName());
                intent.putExtra("phone", branches.get(4).getPhoneNumber());
                intent.putExtra("address", branches.get(4).getAddress());
                intent.putExtra("imgURL", branches.get(4).getImageUrl());
                intent.putExtra("openTime", branches.get(4).getOpeningTime());
                intent.putExtra("closeTime", branches.get(4).getClosingTime());
                intent.putExtra("isSell", branches.get(4).isSell());
                intent.putExtra("lat", branches.get(4).getLatLng().latitude);
                intent.putExtra("lng", branches.get(4).getLatLng().longitude);
                startActivity(intent);
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        tvMainMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BranchActivity.class);
                intent.putExtra("categoryId", 8);
                startActivity(intent);
            }
        });
        tvSnacks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BranchActivity.class);
                intent.putExtra("categoryId", 9);
                startActivity(intent);
            }
        });
        tvDrinks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BranchActivity.class);
                intent.putExtra("categoryId", 10);
                startActivity(intent);
            }
        });
        tvTraSua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BranchActivity.class);
                intent.putExtra("categoryId", 1);
                startActivity(intent);
            }
        });
        tvBurgers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BranchActivity.class);
                intent.putExtra("categoryId", 7);
                startActivity(intent);
            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                mLocation = locationResult.getLastLocation();

                updateUI();

                stopLocationUpdates();
            }
        };

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleSmall);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        layoutProgressBar.addView(progressBar, params);
        progressBar.setVisibility(View.VISIBLE);

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartDialog cartDialog = new CartDialog(Objects.requireNonNull(getActivity()), R.style.CartDialog);
                cartDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                cartDialog.show();
            }
        });

        ibQRScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, RequestsCode.REQUEST_CAMERA_PERMISSION);
                    Toast.makeText(getActivity(), "camera permission denied", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intentQRScanner = new Intent(getActivity(), QRScannerActivity.class);
                startActivityForResult(intentQRScanner, RequestsCode.REQUEST_QR_SCAN);
            }
        });
    }

    public int getUserId(String token) {
        String TOKEN_PREFIX = "Bearer ";
        JWT jwt = new JWT(token.replace(TOKEN_PREFIX, ""));
        username = jwt.getSubject();
        Claim claim = jwt.getClaim("userId");
        return claim.asInt();
    }

    public String getUsername(String token) {
        String TOKEN_PREFIX = "Bearer ";
        JWT jwt = new JWT(token.replace(TOKEN_PREFIX, ""));
        return jwt.getSubject();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RequestsCode.REQUEST_ADDRESS:
                if (resultCode == RESULT_OK && data != null) {
                    tvAddress.setText(data.getStringExtra(Shared.KEY_ADDRESS));
                    // update location
                    if (token.contains("Bearer")) {
                        double lat = data.getDoubleExtra(Shared.KEY_LATITUDE, 0);
                        double lng = data.getDoubleExtra(Shared.KEY_LONGITUDE, 0);

                        // update current location
                        if (mLocation == null) mLocation = new Location("");

                        mLocation.setLatitude(lat);
                        mLocation.setLongitude(lng);

                        // show suggest branch for current location
                        new GetBranch().execute();

                        String address = data.getStringExtra(Shared.KEY_ADDRESS);
                        new UpdateLocationTask(getActivity(), lat, lng, address, consumerID).execute();
                        // store consumer address info
                        SharedPreferences.Editor editor = Objects.requireNonNull(getActivity()).getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE).edit();
                        editor.putString(Shared.KEY_ADDRESS, address);
                        editor.putString(Shared.KEY_LATITUDE, lat + "");
                        editor.putString(Shared.KEY_LONGITUDE, lng + "");
                        editor.apply();
                    }
                }
                break;
            case RequestsCode.REQUEST_LOCATION:
                Log.i("HomeFragment", "REQUEST_LOCATION");
                if (resultCode == RESULT_OK) {
                    createLocationRequest();
                }
                break;

            case RequestsCode.REQUEST_QR_SCAN:
                if (resultCode == RESULT_OK && data != null) {
                    String dataScanned = data.getStringExtra(Shared.KEY_QR_CODE);
                    assert dataScanned != null;
                    Log.i("dataScanned", dataScanned);

                    try {
                        JSONObject jsonObject = new JSONObject(dataScanned);
                        if (jsonObject.getString("task").equals("showbranch")){
                            JSONObject jsonBranch = jsonObject.getJSONObject("data");
                            Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                            intent.putExtra("id", jsonBranch.getInt("id"));
                            intent.putExtra("name", jsonBranch.getString("name"));
                            intent.putExtra("phone", jsonBranch.getString("phone"));
                            intent.putExtra("address", jsonBranch.getString("address"));
                            intent.putExtra("imgURL", jsonBranch.getString("imageURL"));
                            intent.putExtra("openTime", jsonBranch.getString("openTime"));
                            intent.putExtra("closeTime", jsonBranch.getString("closeTime"));
                            intent.putExtra("isSell", jsonBranch.getBoolean("isSell"));
                            intent.putExtra("lat", jsonBranch.getDouble("lat"));
                            intent.putExtra("lng", jsonBranch.getDouble("lng"));
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), getString(R.string.invalid_qr_code), Toast.LENGTH_LONG).show();

                        try {
                            if (!dataScanned.startsWith("http://") && !dataScanned.startsWith("https://")){
                                dataScanned = "http://" + dataScanned;
                            }
                            Uri uri = Uri.parse(dataScanned);
                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        } catch (Exception ex){
                            Toast.makeText(getActivity(), getString(R.string.cannot_open_link)+"\nerror: "+ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RequestsCode.REQUEST_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createLocationRequest();
            Log.i("HomeFragment", "onRequestPermissionsResult");
        } else if (requestCode == RequestsCode.REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Intent intentQRScanner = new Intent(getActivity(), QRScannerActivity.class);
            startActivityForResult(intentQRScanner, RequestsCode.REQUEST_QR_SCAN);
        }
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // GPS

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        final SettingsClient client = LocationServices.getSettingsClient(Objects.requireNonNull(getActivity()));
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize location requests here.
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

                getLastLocation();

                if (mLocation == null)
                    startLocationUpdates();
            }
        });

        task.addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("HomeFragment", "no permission, resolving...");
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        startIntentSenderForResult(resolvable.getResolution().getIntentSender(), RequestsCode.REQUEST_LOCATION, null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException sendEx) {
                        sendEx.printStackTrace();
                    }
                }
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.i("HomeFragment", "getLastLocation SUCCESS");
                        if (location != null){
                            Log.i("HomeFragment", "Location: "+location.getLatitude()+","+location.getLongitude());
                            mLocation = location;
                            updateUI();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("HomeFragment", "lastLocation not available");
                    }
                });
    }

    private Location getLastBestLocationByGPS() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    private void updateUI(){
        if (mLocation != null) {

            // show suggest branch
            new GetBranch().execute();

            new LocationTask().execute();
        }
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        Log.d(TAG, "-->onSliderClick | "+slider.getBundle().get("extra"));
        Intent intent = null;
        switch (slider.getBundle().get("extra").toString()){
            case "Ăn sáng":
                intent = new Intent(getActivity(), BranchActivity.class);
                intent.putExtra("categoryId", 8);
                startActivity(intent);
                break;

            case "Ăn vặt":
                intent = new Intent(getActivity(), BranchActivity.class);
                intent.putExtra("categoryId", 9);
                startActivity(intent);
                break;

            case "Trà sữa":
                intent = new Intent(getActivity(), BranchActivity.class);
                intent.putExtra("categoryId", 1);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onStop() {
        sliderLayout.stopAutoCycle();
        super.onStop();
    }

    @SuppressLint("StaticFieldLeak")
    class LocationTask extends AsyncTask<String,String,String>{

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            addressLine = getActivity().getString(R.string.choose_delivery_location);

            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
                Address address = addresses.get(0);
                addressLine = address.getAddressLine(0);

                // save consumer location
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE).edit();
                editor.putString(Shared.KEY_ADDRESS, addressLine);
                editor.putString(Shared.KEY_LATITUDE, mLocation.getLatitude()+"");
                editor.putString(Shared.KEY_LONGITUDE, mLocation.getLongitude()+"");
                editor.apply();
            } catch (IOException e) {
                Log.i("HomeFragment", "errorGeocoder: " + e.getMessage());
            }
            return addressLine;
        }

        @Override
        protected void onPostExecute(String s) {
            progressBar.setVisibility(View.INVISIBLE);

            tvAddress.setText(s);

            if (token.contains("Bearer")) {
                new ConsumerTask().execute();
            }
            super.onPostExecute(s);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressLint("StaticFieldLeak")
    class ConsumerTask extends AsyncTask<String, String, String> {
        private InputStream is;
        private int resultCode;
        @Override
        protected String doInBackground(String... strings) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(RequestUrl.CONSUMER + "user/" +userId);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", token);
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.connect();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", ""+statusCode);
                if (statusCode >= 200 && statusCode < 400){
                    resultCode = ResultsCode.SUCCESS;
                    is = connection.getInputStream();
                } else {
                    resultCode = ResultsCode.FAILED;
                    is = connection.getErrorStream();
                }

                reader = new BufferedReader(new InputStreamReader(is));

                StringBuilder buffer = new StringBuilder();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    Log.d("ResponseConsumer: ", "> " + line);
                }

                return buffer.toString();
            } catch (SocketTimeoutException e) {
                resultCode = ResultsCode.SOCKET_TIMEOUT;
            } catch (IOException e){
                resultCode = ResultsCode.FAILED;
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch (resultCode) {
                case ResultsCode.SUCCESS:
                    try {
                        JSONObject consumer = new JSONObject(s);
                        consumerID = consumer.getInt("id");
                        consumerLocation = consumer.getString("consumerLocation");
                        if (consumerLocation.equals("null")) {
                            new SaveLocationTask(getActivity(), mLocation.getLatitude(), mLocation.getLongitude(), addressLine, consumerID).execute();
                        } else {
                            new UpdateLocationTask(getActivity(), mLocation.getLatitude(), mLocation.getLongitude(), addressLine, consumerID).execute();
                        }

                        // get cartId
                        new GetCartTask().execute(token, consumerID + "");

                        // store some consumer info
                        SharedPreferences.Editor editor = Objects.requireNonNull(getActivity()).getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE).edit();
                        editor.putInt(Shared.KEY_CONSUMER_ID, consumerID);
                        editor.putString(Shared.KEY_USERNAME, username);
                        editor.putString(Shared.KEY_FIRST_NAME, consumer.getString("firstName"));
                        editor.putString(Shared.KEY_LAST_NAME, consumer.getString("lastName"));
                        editor.putString(Shared.KEY_PHONE, consumer.getString("phoneNumber"));
                        if (!consumerLocation.equals("null")) {
                            JSONObject jsonLocation = new JSONObject(consumerLocation);
                            editor.putString(Shared.KEY_ADDRESS, jsonLocation.getString("address"));
                            editor.putString(Shared.KEY_LATITUDE, jsonLocation.getDouble("latitude") + "");
                            editor.putString(Shared.KEY_LONGITUDE, jsonLocation.getDouble("longitude") + "");
                        }
                        editor.apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
                    break;

                case ResultsCode.FAILED:
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class SaveLocationTask extends AsyncTask<String,String,String> {
        private Context context;
        private OutputStream os;
        private InputStream is;
        private double latitude, longitude;
        private String address;
        private int consumerID;
        private final String locationURL = "https://orefoo.herokuapp.com/consumer-location";

        public SaveLocationTask(Context context, double latitude, double longitude, String address, int consumerID){
            this.context = context;
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.consumerID = consumerID;
        }

        @Override
        protected String doInBackground(String... strings) {
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
                    Log.d("ResponseSaveLocation: ", "> " + line);
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

    @SuppressLint("StaticFieldLeak")
    class UpdateLocationTask extends AsyncTask<String,String,String> {
        private Context context;
        private OutputStream os;
        private InputStream is;
        private double latitude, longitude;
        private String address;
        private int consumerID;
        private final String locationURL = "https://orefoo.herokuapp.com/consumer-location?consumer-id=";

        public UpdateLocationTask(Context context, double latitude, double longitude, String address, int consumerID){
            this.context = context;
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.consumerID = consumerID;
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(locationURL+consumerID);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("latitude", latitude);
                jsonObject.put("longitude", longitude);
                jsonObject.put("address", address);
                String data = jsonObject.toString();
                Log.d("test", "update location: "+data);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PATCH");
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
                    Log.d("ResponseUpdateLocation", "> " + line);
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
        }
    }

    @SuppressLint("StaticFieldLeak")
    class GetBranch extends AsyncTask<String,String,String> {
        private InputStream is;
        private int resultCode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(RequestUrl.BRANCH + "?page=1");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", token);
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.connect();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+"");
                if (statusCode >= 200 && statusCode < 400){
                    resultCode = ResultsCode.SUCCESS;
                    is = connection.getInputStream();
                } else {
                    resultCode = statusCode;
                    is = connection.getErrorStream();
                }

                reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                    Log.d("ResponseGetBranch: ", "> " + line);
                }

                return buffer.toString();
            } catch (SocketTimeoutException e) {
                resultCode = ResultsCode.SOCKET_TIMEOUT;
            } catch (IOException e){
                resultCode = ResultsCode.IO_EXCEPTION;
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressBar.setVisibility(View.INVISIBLE);

            if (s == null) return;

            switch (resultCode){
                case ResultsCode.SUCCESS:

                    try {
                        branches.clear();
                        // add branches
                        JSONArray jsonArray = new JSONArray(s);
                        for (int i = 0; i < Math.min(jsonArray.length(), 5); i++){
                            final JSONObject jsonObject = jsonArray.getJSONObject(i);
                            branches.add(new Branch(jsonObject.getInt("id"), jsonObject.getString("name"), jsonObject.getString("phoneNumber"),
                                    jsonObject.getString("imageUrl"), jsonObject.getString("openingTime"), jsonObject.getString("closingTime"),
                                    jsonObject.getString("address"), new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")),
                                    jsonObject.getBoolean("isSell"), jsonObject.getInt("merchant"), jsonObject.getString("branchStatus")));
                        }

                        // sort branch following distance
                        if (mLocation != null) Collections.sort(branches, new SortPlaces(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())));
                        else {
                            Toast.makeText(getActivity(), "Cannot get current location, try again", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // show branch
                        LatLng currentLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                        for (int index = 0; index<(Math.min(jsonArray.length(), 5)); index++){
                            switch (index){
                                case 0:
                                    updateUIBranchSuggest(getActivity(), ivItem1, tvItemName1, tvKm1, tvIsSell1,
                                            branches.get(0).getImageUrl(), branches.get(0).getName(), GetDistanceInKmString(currentLocation, branches.get(0).getLatLng()), branches.get(0).isSell(), isOpening(branches.get(0).getOpeningTime(), branches.get(0).getClosingTime()));
                                    break;

                                case 1:
                                    updateUIBranchSuggest(getActivity(), ivItem2, tvItemName2, tvKm2, tvIsSell2,
                                            branches.get(1).getImageUrl(), branches.get(1).getName(), GetDistanceInKmString(currentLocation, branches.get(1).getLatLng()), branches.get(1).isSell(), isOpening(branches.get(1).getOpeningTime(), branches.get(1).getClosingTime()));
                                    break;

                                case 2:
                                    updateUIBranchSuggest(getActivity(), ivItem3, tvItemName3, tvKm3, tvIsSell3,
                                            branches.get(2).getImageUrl(), branches.get(2).getName(), GetDistanceInKmString(currentLocation, branches.get(2).getLatLng()), branches.get(2).isSell(), isOpening(branches.get(2).getOpeningTime(), branches.get(2).getClosingTime()));
                                    break;

                                case 3:
                                    updateUIBranchSuggest(getActivity(), ivItem4, tvItemName4, tvKm4, tvIsSell4,
                                            branches.get(3).getImageUrl(), branches.get(3).getName(), GetDistanceInKmString(currentLocation, branches.get(3).getLatLng()), branches.get(3).isSell(), isOpening(branches.get(3).getOpeningTime(), branches.get(3).getClosingTime()));
                                    break;

                                case 4:
                                    updateUIBranchSuggest(getActivity(), ivItem5, tvItemName5, tvKm5, tvIsSell5,
                                            branches.get(4).getImageUrl(), branches.get(4).getName(), GetDistanceInKmString(currentLocation, branches.get(4).getLatLng()), branches.get(4).isSell(), isOpening(branches.get(4).getOpeningTime(), branches.get(4).getClosingTime()));
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
                    break;
                case ResultsCode.IO_EXCEPTION:
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.io_exception), Toast.LENGTH_SHORT).show();
                    break;
                case 422:
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.token_expired), Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE).edit();
                    editor.remove(Shared.KEY_BEARER);
                    editor.apply();
                    break;
                default:
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void updateUIBranchSuggest(Context context, ImageView branchImage, TextView branchName, TextView branchDistance, TextView tvIsSell, String imageUrl, String name, String distance, boolean isSell, boolean isOpening){
        // Branch image
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.image_not_found);
        Glide.with(context).load(imageUrl).apply(options).into(branchImage);

        // Branch name
        branchName.setText(name);

        // Distance from Branch to Consumer location
        branchDistance.setText(distance);

        // is sell
        if (isSell) {
            tvIsSell.setVisibility(View.INVISIBLE);
        } else {
            tvIsSell.setText(getString(R.string.not_open_for_sale));
            tvIsSell.setVisibility(View.VISIBLE);
        }

        // is opening
        if (!isSell) return;
        if (isOpening){
            tvIsSell.setVisibility(View.INVISIBLE);

            switch (tvIsSell.getId()){
                case R.id.tvIsSell1:
                    cvItem1.setEnabled(true);
                    break;
                case R.id.tvIsSell2:
                    cvItem2.setEnabled(true);
                    break;
                case R.id.tvIsSell3:
                    cvItem3.setEnabled(true);
                    break;
                case R.id.tvIsSell4:
                    cvItem4.setEnabled(true);
                    break;
                case R.id.tvIsSell5:
                    cvItem5.setEnabled(true);
                    break;
            }
        } else {
            tvIsSell.setText(getString(R.string.temp_close));
            tvIsSell.setVisibility(View.VISIBLE);
        }
    }

    public boolean isOpening(String openingTime, String closingTime){
        String[] from = openingTime.split(":");
        String[] until = closingTime.split(":");

        Calendar fromTime = Calendar.getInstance();
        fromTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(from[0]));
        fromTime.set(Calendar.MINUTE, Integer.parseInt(from[1]));
        Log.d(TAG, "from: "+fromTime.getTime().toString());

        Calendar toTime = Calendar.getInstance();
        toTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(until[0]));
        toTime.set(Calendar.MINUTE, Integer.parseInt(until[1]));
        Log.d(TAG, "until: "+toTime.getTime().toString());

        Calendar currentTime = Calendar.getInstance();
        Log.d(TAG, "current: "+currentTime.getTime().toString());

        return !(currentTime.before(fromTime) || currentTime.after(toTime));
    }

    public double rad(double x){
        return x * Math.PI / 180;
    }
    public double GetDistance(LatLng p1, LatLng p2){
        double R = 6378137; // Earth’s mean radius in meter
        double dLat = rad(p2.latitude - p1.latitude);
        double dLng = rad(p2.longitude - p1.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(rad(p1.latitude)) * Math.cos(rad(p2.latitude)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    @SuppressLint("DefaultLocale")
    public String GetDistanceInKmString(LatLng p1, LatLng p2){
        return String.format("%.1f", GetDistance(p1, p2)/1000) + "Km";
    }

    // Get Cart Item Size
    @SuppressLint("StaticFieldLeak")
    class CartItemTask extends AsyncTask<String,String,String> {
        private InputStream is;
        private int cartID;
        private final String cartItemURL = "https://orefoo.herokuapp.com/cart-item?cart-id=";
        private int resultCode;

        public void getAllItem(int cartID){
            this.cartID = cartID;
            if (cartID != -1) execute();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(cartItemURL + cartID);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", token);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.connect();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+"");

                if (statusCode >= 200 && statusCode < 400){
                    is = connection.getInputStream();
                    resultCode = ResultsCode.SUCCESS;
                } else {
                    is = connection.getErrorStream();
                    if (statusCode == 406) {
                        resultCode = ResultsCode.DIFFERENCE_BRANCH;
                    } else resultCode = ResultsCode.FAILED;
                }

                reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                    Log.d("ResponseCartItem: ", "> " + line);
                }

                return buffer.toString();
            } catch (SocketTimeoutException e) {
                resultCode = ResultsCode.SOCKET_TIMEOUT;
            } catch (IOException e){
                resultCode = ResultsCode.IO_EXCEPTION;
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressBar.setVisibility(View.INVISIBLE);

            String sizeCart = "0";
            tvSizeOfCart.setText(sizeCart);

            switch (resultCode) {
                case ResultsCode.SUCCESS:
                    Log.i("result", "get cart item success");
                    try {
                        JSONArray jsonArray = new JSONArray(s);
                        sizeCart = jsonArray.length()+"";
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case ResultsCode.DIFFERENCE_BRANCH:
                    break;
                case ResultsCode.FAILED:
                    Log.i("result", "get failed");
                    break;
                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.socket_timeout), Toast.LENGTH_SHORT).show();
                    break;
                case ResultsCode.IO_EXCEPTION:
                    Toast.makeText(getContext(), "IO Exception", Toast.LENGTH_SHORT).show();
                    break;
            }
            tvSizeOfCart.setText(sizeCart);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // show size cart to Cart Button
        if (token.contains("Bearer"))
            new CartItemTask().getAllItem(cartId);
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // Get cartId //////////////////
    @SuppressLint("StaticFieldLeak")
    class GetCartTask extends AsyncTask<String,String,String> {
        private InputStream is;
        private final String victualsURL = "https://orefoo.herokuapp.com/cart?consumer-id=";
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(victualsURL + strings[1]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", strings[0]);
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.connect();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+"");
                if (statusCode >= 200 && statusCode < 400){
                    is = connection.getInputStream();
                } else {
                    is = connection.getErrorStream();
                }

                reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                    Log.d("ResponseGetCartTask: ", "> " + line);
                }

                return buffer.toString();
            } catch (SocketTimeoutException e) {
                Log.i("MenuFragment", e.getMessage());
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) return;

            try {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(Shared.CART, Context.MODE_PRIVATE).edit();
                JSONObject jsonObject = new JSONObject(s);
                editor.putInt(Shared.KEY_CART_ID, jsonObject.getInt("id"));
                editor.apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}