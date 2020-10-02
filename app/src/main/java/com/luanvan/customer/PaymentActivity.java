package com.luanvan.customer;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.luanvan.customer.components.MoMoConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import vn.momo.momo_partner.AppMoMoLib;
import vn.momo.momo_partner.MoMoParameterNamePayment;

public class PaymentActivity extends Activity {
    TextView tvEnvironment;
    TextView tvMerchantCode;
    TextView tvMerchantName;
    EditText edAmount;
    TextView tvMessage;
    Button btnPayMoMo;
    private String amount = "10000";
    private String fee = "0";
    int environment = 1;//developer default
    private String merchantName = "OraFood";
    private String merchantCode = "MOMO2GMX20200905";
    private String transId = "25081998";
    private String merchantNameLabel = "Nhà cung cấp";
    private String description = "Thanh toán MoMo";
    private String storeId = "";
    private String storeName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvEnvironment = findViewById(R.id.tvEnvironment);
        tvMerchantCode = findViewById(R.id.tvMerchantCode);
        tvMerchantName = findViewById(R.id.tvMerchantName);
        edAmount = findViewById(R.id.edAmount);
        tvMessage = findViewById(R.id.tvMessage);
        btnPayMoMo = findViewById(R.id.btnPayMoMo);

        Bundle data = getIntent().getExtras();
        if(data != null){
            environment = data.getInt(MoMoConstants.KEY_ENVIRONMENT);
        }
        if(environment == 0){
            AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEBUG);
            tvEnvironment.setText("Development Environment");
        }else if(environment == 1){
            AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT);
            tvEnvironment.setText("Development Environment");
        }else if(environment == 2){
            AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.PRODUCTION);
            tvEnvironment.setText("PRODUCTION Environment");
        }

        tvMerchantCode.setText("Merchant Code: "+merchantCode);
        tvMerchantName.setText("Merchant Name: "+merchantName);

        btnPayMoMo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPayment();
            }
        });
    }

    //example payment
    private void requestPayment() {
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT);
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN);

        if (!edAmount.getText().toString().equals("") && edAmount.getText().toString().trim().length() != 0)
            amount = edAmount.getText().toString().trim();

        Map<String, Object> eventValue = new HashMap<>();
        //client Required
        eventValue.put(MoMoParameterNamePayment.MERCHANT_NAME, merchantName);
        eventValue.put(MoMoParameterNamePayment.MERCHANT_CODE, merchantCode);
        eventValue.put(MoMoParameterNamePayment.AMOUNT, amount);
        eventValue.put(MoMoParameterNamePayment.DESCRIPTION, description);
        //client Optional
        eventValue.put(MoMoParameterNamePayment.FEE, fee);
        eventValue.put(MoMoParameterNamePayment.MERCHANT_NAME_LABEL, merchantNameLabel);

        eventValue.put(MoMoParameterNamePayment.REQUEST_ID,  merchantCode+"-"+ UUID.randomUUID().toString());
        eventValue.put(MoMoParameterNamePayment.PARTNER_CODE, "MOMO2GMX20200905");

        JSONObject objExtraData = new JSONObject();
        try {
            objExtraData.put("site_code", "008");
            objExtraData.put("site_name", "CGV Cresent Mall");
            objExtraData.put("screen_code", 0);
            objExtraData.put("screen_name", "Special");
            objExtraData.put("movie_name", "Kẻ Trộm Mặt Trăng 3");
            objExtraData.put("movie_format", "2D");
            objExtraData.put("ticket", "{\"ticket\":{\"01\":{\"type\":\"std\",\"price\":110000,\"qty\":3}}}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        eventValue.put(MoMoParameterNamePayment.EXTRA_DATA, objExtraData.toString());
        eventValue.put(MoMoParameterNamePayment.REQUEST_TYPE, "payment");
        eventValue.put(MoMoParameterNamePayment.LANGUAGE, "vi");
        eventValue.put(MoMoParameterNamePayment.EXTRA, "");
        //Request momo app
        AppMoMoLib.getInstance().requestMoMoCallBack(this, eventValue);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == -1) {
            if(data != null) {
                if(data.getIntExtra("status", -1) == 0) {
                    tvMessage.setText("message: " + "Get token " + data.getStringExtra("message"));

                    if(data.getStringExtra("data") != null && !data.getStringExtra("data").equals("")) {
                        // TODO:
                        try {
                            JSONObject jsonData = new JSONObject();
                            jsonData.put("partnerCode", merchantCode);
                            jsonData.put("partnerName", merchantName);
                            jsonData.put("transId", transId);
                            jsonData.put("amount", amount);
                            jsonData.put("phonenumber", data.getStringExtra("phonenumber"));
                            jsonData.put("data", data.getStringExtra("data"));
                            jsonData.put("storeId", storeId);
                            jsonData.put("storeName", storeName);
                            jsonData.put("description", description);

                            new PayTask(jsonData.toString()).execute();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                    }
                } else if(data.getIntExtra("status", -1) == 1) {
                    String message = data.getStringExtra("message") != null?data.getStringExtra("message"):"Thất bại";
                    tvMessage.setText("message: " + message);
                } else if(data.getIntExtra("status", -1) == 2) {
                    tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                } else {
                    tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                }
            } else {
                tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
            }
        } else {
            tvMessage.setText("message: " + this.getString(R.string.not_receive_info_err));
        }
    }

    private class PayTask extends AsyncTask<Void,String,String>{
        BufferedReader reader = null;
        OutputStream os;
        InputStream is;
        private String jsonString;
        public PayTask(String jsonString){
            this.jsonString = jsonString;
        }
        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            //http post
            try {
                URL url = new URL("https://orafood-momopay.herokuapp.com/pay");

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.setDoOutput(true);
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(30000);
                connection.setFixedLengthStreamingMode(jsonString.getBytes().length);
                connection.connect();

                os = new BufferedOutputStream(connection.getOutputStream());
                os.write(jsonString.getBytes());
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
            JSONObject jsonObject = null;
            if (s != null){
                try {
                    jsonObject = new JSONObject(s);
                    if (jsonObject.getInt("status") == 0)
                        Toast.makeText(PaymentActivity.this, "Thanh toán thành công "+jsonObject.getString("amount")+"VND", Toast.LENGTH_LONG).show();
                    else Toast.makeText(PaymentActivity.this, "Thanh toán thất bại", Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(PaymentActivity.this, "Thanh toán thất bại", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}