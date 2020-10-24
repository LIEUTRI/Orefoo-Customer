package com.luanvan.customer.components;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.luanvan.customer.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CalculateDistanceTime {

    private taskCompleteListener mTaskListener;
    private Context mContext;
    private final String TAG = "CalculateDistanceTime";

    public CalculateDistanceTime(Context context) {
        mContext = context;
    }

    public void setLoadListener(taskCompleteListener taskListener) {
        mTaskListener = taskListener;
    }


    public void getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        String APIKey = mContext.getResources().getString(R.string.google_maps_key);
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters+"&key="+APIKey+"&mode=DRIVING";

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d(TAG, e.toString());
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data;
    }


    public interface taskCompleteListener {
        void taskCompleted(String[] time_distance);
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<HashMap<String, String>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DistanceTimeParser parser = new DistanceTimeParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            String duration_distance = "";

            if (result == null || result.size() < 1) {
                Log.e(TAG, "No Points found");
                return;
            }

            String[] date_dist = new String[2];

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {

                // Fetching i-th route
                HashMap<String, String> tmpData = result.get(i);
                Set<String> key = tmpData.keySet();
                Iterator<String> it = key.iterator();
                while (it.hasNext()) {
                    String hmKey = (String) it.next();
                    duration_distance = tmpData.get(hmKey);

                    System.out.println("Key: " + hmKey + " & Data: " + duration_distance);

                    it.remove(); // avoids a ConcurrentModificationException
                }

                date_dist[i] = duration_distance;
            }

            mTaskListener.taskCompleted(date_dist);
        }
    }
}
