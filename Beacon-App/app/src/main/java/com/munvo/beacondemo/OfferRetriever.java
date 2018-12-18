package com.munvo.beacondemo;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.munvo.beaconlocate.ble.beacon.Beacon;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

//import java.util.jar.Manifest;


public class OfferRetriever {

    long lastOfferRefresh;
    String json;


    public int getOffers() {

        if (pullOffers())
            lastOfferRefresh = System.currentTimeMillis();

        /*
        final TextView mTextView = (TextView) findViewById(R.id.text);
// ...

                        // Display the first 500 characters of the response string.
                        mTextView.setText("Response is: "+ response.substring(0,500));
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work!");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);*/

        return 0;
    }

    public boolean pullOffers() {
        // Change to
        DownloadTask task = new DownloadTask();
        task.execute("https://kafka-rest-prod02.messagehub.services.us-south.bluemix.net:443/topics/offers");
        return true;
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpsURLConnection urlConnection = null;
            try {

                url = new URL(urls[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("X-Auth-Token", "XAQ1MnwCNySJwPWziVsJXDKKlo8bVrV440D70HfiMfqR9oJQ");
                urlConnection.setRequestProperty("Content-Type", "application/vnd.kafka.binary.v1+json");

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.writeBytes(json);
                wr.flush();
                wr.close();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d("Message Sent", s);

        }
    }

}
