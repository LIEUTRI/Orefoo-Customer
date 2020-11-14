package com.luanvan.customer.components;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.luanvan.customer.Fragments.MenuFragment;
import com.luanvan.customer.R;

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
import java.util.Objects;

public class AddItemDialog extends Dialog {
    private String id;
    private int itemId;
    private String imageUrl;
    private String name;
    private double price, priceOrigin;
    private Activity activity;

    private ImageView ivVictual;
    private TextView tvName;
    private TextView tvPrice, tvPriceOrigin;
    private ImageButton btnClose;
    private ImageButton btnAdd, btnRemove;
    private TextView tvQuantity;
    private TextView btnAddItem;

    private double totalPrice = 0.0;
    private double totalPriceOrigin = 0.0;

    private int quantity = 1;
    private int curQuantity;

    private int cartId;
    private String token;
    private LatLng consumerLatLng, branchLatLng;
    private AlertDialog dialogBranch, dialogDistance;
    private JSONObject json;

    public AddItemDialog(@NonNull Activity activity, int themeResId, String imageUrl, String name, double price, double priceOrigin, String id, int curQuantity, int itemId) {
        super(activity, themeResId);
        this.activity = activity;
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.priceOrigin = priceOrigin;
        this.id = id;
        this.curQuantity = curQuantity;
        this.itemId = itemId;
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_layout);

        ivVictual = findViewById(R.id.ivVictual);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvPriceOrigin = findViewById(R.id.tvPriceOrigin);
        btnClose = findViewById(R.id.btnClose);
        btnAdd = findViewById(R.id.btnAdd);
        btnRemove = findViewById(R.id.btnRemove);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnAddItem = findViewById(R.id.btnAddItem);

        totalPrice = price;
        totalPrice = priceOrigin;
        String add = activity.getResources().getString(R.string.add)+" "+String.format("%,.0f", price)+"đ";
        btnAddItem.setText(add);


        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.image_not_found);
        Glide.with(activity).load(imageUrl).apply(options).into(ivVictual);
        tvName.setText(name);
        tvPrice.setText(String.format("%,.0f", price));
        String priceOriginString = String.format("%,.0f", priceOrigin);
        tvPriceOrigin.setPaintFlags(tvPriceOrigin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        tvPriceOrigin.setText(price==priceOrigin ? "":priceOriginString);

        //////////////////////////////////////////////////////
        SharedPreferences sharedPreferences = activity.getSharedPreferences(Shared.CART, Context.MODE_PRIVATE);
        cartId = sharedPreferences.getInt(Shared.KEY_CART_ID, -1);
        Log.i("cartidAddItemDialog", cartId+"");

        sharedPreferences = activity.getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "");

        // consumer location
        sharedPreferences = activity.getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE);
        consumerLatLng = new LatLng(Double.parseDouble(sharedPreferences.getString(Shared.KEY_LATITUDE, "0")),
                Double.parseDouble(sharedPreferences.getString(Shared.KEY_LONGITUDE, "0")));

        // branch location
        sharedPreferences = activity.getSharedPreferences(Shared.BRANCH, Context.MODE_PRIVATE);
        branchLatLng = new LatLng(Double.parseDouble(sharedPreferences.getString(Shared.KEY_LATITUDE, "0")),
                Double.parseDouble(sharedPreferences.getString(Shared.KEY_LONGITUDE, "0")));
        //////////////////////////////////////////////////////

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                quantity++;
                tvQuantity.setText(quantity+"");
                totalPrice += price;
                totalPriceOrigin += priceOrigin;
                String add = activity.getResources().getString(R.string.add)+" "+String.format("%,.0f", totalPrice)+"đ";
                btnAddItem.setText(add);
            }
        });
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (quantity == 1) {
                    return;
                }
                quantity--;
                tvQuantity.setText(quantity+"");
                totalPrice -= price;
                totalPriceOrigin -= priceOrigin;
                String add = activity.getResources().getString(R.string.add)+" "+String.format("%,.0f", totalPrice)+"đ";
                btnAddItem.setText(add);
            }
        });

        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add to cart
                if (curQuantity >= 1){
                    new UpdateQuantityTask().execute(itemId+"", (quantity+curQuantity)+"");
                } else {
                    json = new JSONObject();
                    try {
                        json.put("quantity", quantity+curQuantity);
                        json.put("cart", new JSONObject().put("id", cartId));
                        json.put("victuals", new JSONObject().put("id", id));

                        CalculateDistanceTime distance_task = new CalculateDistanceTime(activity);
                        distance_task.getDirectionsUrl(consumerLatLng, branchLatLng);
                        distance_task.setLoadListener(new CalculateDistanceTime.taskCompleteListener() {
                            @Override
                            public void taskCompleted(String[] distance) {
                                if (Float.parseFloat(distance[0].substring(0,distance[0].indexOf(" "))) > 10.0){
                                    showDialogDistance();
                                } else {
                                    new AddToCartTask().execute(json.toString());

                                    double km = Double.parseDouble(distance[0].substring(0,distance[0].indexOf(" ")));
                                    SharedPreferences.Editor editor = activity.getSharedPreferences(Shared.BRANCH, Context.MODE_PRIVATE).edit();
                                    editor.putFloat(Shared.KEY_BRANCH_DISTANCE, (float) km);
                                    editor.apply();
                                    new DistanceTask().postDistance(cartId, km);
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                cancel();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    class AddToCartTask extends AsyncTask<String,String,String> {
        private InputStream is;
        private OutputStream os;
        private int resultCode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MenuFragment.progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(RequestUrl.CART_ITEM);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", token);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.connect();

                Log.i("request", strings[0]);
                os = new BufferedOutputStream(connection.getOutputStream());
                os.write(strings[0].getBytes());
                os.flush();

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
                    Log.d("Response: ", "> " + line);
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            MenuFragment.progressBar.setVisibility(View.INVISIBLE);

            if (s == null) return;

            switch (resultCode) {
                case ResultsCode.SUCCESS:
                    Log.i("result", "add success");
                    activity.finish();
                    activity.startActivity(activity.getIntent());
                    activity.overridePendingTransition(0,0);
                    break;
                case ResultsCode.DIFFERENCE_BRANCH:
                    showDialogBranch();
                    break;
                case ResultsCode.FAILED:
                    Log.i("result", "add failed");
                    break;
                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(activity, activity.getResources().getString(R.string.socket_timeout), Toast.LENGTH_SHORT).show();
                    break;
                case ResultsCode.IO_EXCEPTION:
                    Toast.makeText(activity, "IO Exception", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    // delete all cart item
    @SuppressLint("StaticFieldLeak")
    class DeleteAllCartItemTask extends AsyncTask<String,String,String> {
        private InputStream is;
        private int resultCode;
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(RequestUrl.CART+strings[0]+"/change-branch");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");
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
                    Log.d("Response: ", "> " + line);
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) return;

            switch (resultCode) {
                case ResultsCode.SUCCESS:
                    Log.i("result", "delete success");
                    new AddToCartTask().execute(json.toString());
                    break;
                case ResultsCode.DIFFERENCE_BRANCH:

                    break;
                case ResultsCode.FAILED:
                    Log.i("result", "delete failed");
                    break;
                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(activity, activity.getResources().getString(R.string.socket_timeout), Toast.LENGTH_SHORT).show();
                    break;
                case ResultsCode.IO_EXCEPTION:
                    Toast.makeText(activity, "IO Exception", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class UpdateQuantityTask extends AsyncTask<String,String,String> {
        private InputStream is;
        private OutputStream os;
        private int resultCode;
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(RequestUrl.CART_ITEM + strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Authorization", token);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.connect();

                try {
                    String requestJson = new JSONObject().put("quantity", strings[1]).toString();
                    Log.i("requestQuantity", requestJson);
                    os = new BufferedOutputStream(connection.getOutputStream());
                    os.write(requestJson.getBytes());
                    os.flush();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
                    Log.d("Response: ", "> " + line);
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) return;

            switch (resultCode) {
                case ResultsCode.SUCCESS:
                    Log.i("result", "update success");
                    activity.finish();
                    activity.startActivity(activity.getIntent());
                    activity.overridePendingTransition(0,0);
                    break;
                case ResultsCode.DIFFERENCE_BRANCH:
                    break;
                case ResultsCode.FAILED:
                    Log.i("result", "update failed");
                    break;
                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(activity, activity.getResources().getString(R.string.socket_timeout), Toast.LENGTH_SHORT).show();
                    break;
                case ResultsCode.IO_EXCEPTION:
                    Toast.makeText(activity, "IO Exception", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    // dialogs/////////////////
    private void showDialogBranch(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getResources().getString(R.string.difference_branch_title));
        builder.setMessage(activity.getResources().getString(R.string.difference_branch_message));
        builder.setCancelable(false);
        builder.setPositiveButton(
                activity.getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteAllCartItemTask().execute(cartId+"");
                    }
                }
        );
        builder.setNegativeButton(
                activity.getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }
        );
        builder.setNeutralButton(
                activity.getResources().getString(R.string.see_cart),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new CartDialog(Objects.requireNonNull(activity), R.style.CartDialog).show();
                    }
                }
        );

        dialogBranch = builder.create();
        dialogBranch.show();
    }
    private void closeDialogBranch(){
        if (dialogBranch != null) dialogBranch.cancel();
    }

    private void showDialogDistance(){
        closeDialogBranch();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getResources().getString(R.string.too_far));
        builder.setMessage(activity.getResources().getString(R.string.too_far_message));
        builder.setCancelable(false);
        builder.setPositiveButton(
                activity.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }
        );

        dialogDistance = builder.create();
        dialogDistance.show();
    }

    // send distance to server
    @SuppressLint("StaticFieldLeak")
    class DistanceTask extends AsyncTask<String,String,String> {
        private InputStream is;
        private int cartID;
        private double km;
        private int resultCode;

        public void postDistance(int cartID, double km){
            this.cartID = cartID;
            this.km = km;
            execute();
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(RequestUrl.CART + cartID + "/ship?km="+km);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PATCH");
                connection.setRequestProperty("Authorization", token);
                connection.connect();

                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+"");

                if (statusCode >= 200 && statusCode < 400){
                    is = connection.getInputStream();
                    resultCode = ResultsCode.SUCCESS;
                    Log.i("result", "post distance success");
                } else {
                    Log.i("result", "post distance failed");
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
                    Log.d("ResponseDistance: ", "> " + line);
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
            if (s == null) return;

            switch (resultCode) {
                case ResultsCode.SUCCESS:
                    Log.i("result", "post distance success");
                    break;
                case ResultsCode.DIFFERENCE_BRANCH:
                    break;
                case ResultsCode.FAILED:
                    Log.i("result", "get failed");
                    break;
                case ResultsCode.SOCKET_TIMEOUT:
                    Toast.makeText(activity, activity.getResources().getString(R.string.socket_timeout), Toast.LENGTH_SHORT).show();
                    break;
                case ResultsCode.IO_EXCEPTION:
                    Toast.makeText(activity, "IO Exception", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
