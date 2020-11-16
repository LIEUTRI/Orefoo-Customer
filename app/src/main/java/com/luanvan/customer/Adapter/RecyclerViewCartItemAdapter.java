package com.luanvan.customer.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.luanvan.customer.CheckoutActivity;
import com.luanvan.customer.R;

import com.luanvan.customer.components.CartDialog;
import com.luanvan.customer.components.CartItem;
import com.luanvan.customer.components.RequestUrl;
import com.luanvan.customer.components.ResultsCode;
import com.luanvan.customer.components.Shared;
import com.luanvan.customer.components.Victual;

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
import java.util.List;

public class RecyclerViewCartItemAdapter extends RecyclerView.Adapter<RecyclerViewCartItemAdapter.ViewHolder>{
    private List<CartItem> list;
    private Activity activity;
    private String token;
    public RecyclerViewCartItemAdapter(Activity activity, List<CartItem> list){
        this.activity = activity;
        this.list = list;
    }
    @NonNull
    @Override
    public RecyclerViewCartItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        SharedPreferences sharedPreferences = activity.getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "");

        View view = LayoutInflater.from(activity).inflate(R.layout.cart_item, parent, false);
        return new RecyclerViewCartItemAdapter.ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewCartItemAdapter.ViewHolder holder, final int position) {
        final CartItem cartItem = list.get(position);
        RequestOptions options = new RequestOptions()
                                    .centerCrop()
                                    .placeholder(R.drawable.loading)
                                    .error(R.drawable.image_not_found);
        Glide.with(activity).load(cartItem.getImageUrl()).apply(options).into(holder.ivVictual);

        final double price = cartItem.getPrice() / cartItem.getQuantity();
        final double discount = cartItem.getDiscount() / cartItem.getQuantity();

        holder.tvName.setText(cartItem.getName());
        holder.tvPrice.setText(String.format("%,.0f", (price - discount))+"đ");
        holder.tvPriceOrigin.setPaintFlags(holder.tvPriceOrigin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.tvPriceOrigin.setText((price-discount)==price ? "":String.format("%,.0f", price)+"đ");
        holder.tvQuantity.setText(cartItem.getQuantity()+"");

        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(holder.tvQuantity.getText().toString());
                quantity += 1;
                holder.tvQuantity.setText(quantity+"");

                updateUIAdd(activity, price, discount);

                new UpdateQuantityTask().execute(cartItem.getId()+"", quantity+"");
            }
        });
        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(holder.tvQuantity.getText().toString());
                if (quantity == 0) return;
                quantity -= 1;
                holder.tvQuantity.setText(quantity+"");

                updateUIRemove(activity, price, discount);

                if (quantity > 0){
                    new UpdateQuantityTask().execute(cartItem.getId()+"", quantity+"");
                } else {
                    new DeleteCartItemTask().execute(cartItem.getId()+"");
                    removeAt(position);
                    // finish activity
                    if (list.size()==0 && activity.getClass().getSimpleName().equals("CheckoutActivity")){
                        activity.finish();
                    }
                }
            }
        });
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void updateUIAdd(Context context, double price, double discount){
        if ("CheckoutActivity".equals(context.getClass().getSimpleName())) {
            CheckoutActivity.totalPriceVictuals += price - discount;
            CheckoutActivity.tvTotal.setText(String.format("%,.0f", CheckoutActivity.totalPriceVictuals) + "đ");
            CheckoutActivity.tvTotalFinal.setText(String.format("%,.0f", CheckoutActivity.totalPriceVictuals+CheckoutActivity.shippingFee) + "đ");
            CheckoutActivity.portion += 1;
            CheckoutActivity.tvTotalVictuals.setText(activity.getResources().getString(R.string.sum)+" ("+CheckoutActivity.portion+" "+activity.getResources().getString(R.string.portion)+")");
        } else {
            CartDialog.totalPriceVictuals += price - discount;
            CartDialog.totalPriceVictualsOrigin += price;
            CartDialog.tvTotal.setText(String.format("%,.0f", CartDialog.totalPriceVictuals) + "đ");
            CartDialog.tvTotalOrigin.setText(CartDialog.totalPriceVictuals == CartDialog.totalPriceVictualsOrigin ? "" : String.format("%,.0f", CartDialog.totalPriceVictualsOrigin) + "đ");
        }
    }
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void updateUIRemove(Context context, double price, double discount){
        if ("CheckoutActivity".equals(context.getClass().getSimpleName())) {
            CheckoutActivity.totalPriceVictuals -= price - discount;
            CheckoutActivity.tvTotal.setText(String.format("%,.0f", CheckoutActivity.totalPriceVictuals) + "đ");
            CheckoutActivity.tvTotalFinal.setText(String.format("%,.0f", CheckoutActivity.totalPriceVictuals+CheckoutActivity.shippingFee) + "đ");
            CheckoutActivity.portion -= 1;
            CheckoutActivity.tvTotalVictuals.setText(activity.getResources().getString(R.string.sum)+" ("+CheckoutActivity.portion+" "+activity.getResources().getString(R.string.portion)+")");
        } else {
            CartDialog.totalPriceVictuals -= price - discount;
            CartDialog.totalPriceVictualsOrigin -= price;
            CartDialog.tvTotal.setText(String.format("%,.0f", CartDialog.totalPriceVictuals) + "đ");
            CartDialog.tvTotalOrigin.setText(CartDialog.totalPriceVictuals == CartDialog.totalPriceVictualsOrigin ? "" : String.format("%,.0f", CartDialog.totalPriceVictualsOrigin) + "đ");
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivVictual;
        public TextView tvName;
        public TextView tvPrice;
        public TextView tvPriceOrigin;
        public ImageButton btnAdd, btnRemove;
        public TextView tvQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVictual = itemView.findViewById(R.id.ivVictual);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvPriceOrigin = itemView.findViewById(R.id.tvPriceOrigin);
            tvQuantity = itemView.findViewById(R.id.tvAmount);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    public void removeAt(int position) {
        list.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, list.size());
    }
    // Update & Delete////////////////////////////////////////
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
                    SharedPreferences sharedPreferences = activity.getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString(Shared.KEY_BEARER, "");
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

    @SuppressLint("StaticFieldLeak")
    class DeleteCartItemTask extends AsyncTask<String,String,String> {
        private InputStream is;
        private int resultCode;
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(RequestUrl.CART_ITEM + strings[0]);
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
}
