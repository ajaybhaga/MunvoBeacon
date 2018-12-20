package com.munvo.beacondemo;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.munvo.beaconlocate.ble.beacon.Beacon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class OfferRetriever {

    private List<String> logBuffer;
    List<String> offerList = new ArrayList<>(5);

    long lastOfferRefresh = 0;
    String json;
    String base64;

    public OfferRetriever(List<String> logBuffer) {
        this.logBuffer = logBuffer;
    }

    public List<String> getOffers() {

        // Every 5 seconds
        if ((System.currentTimeMillis()-lastOfferRefresh) > 5000.0f) {
            if (pullOffers())
            logBuffer.remove(0);
//            offerList.remove(0);

            lastOfferRefresh = System.currentTimeMillis();
        } else {
            String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
       //     logBuffer.add("[" + timeStamp + "] Waiting for 5 second refresh delay.");
        }



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

        return offerList;
    }

    public boolean pullOffers() {

        String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

        logBuffer.add("[" + timeStamp + "] Retrieving any available offers.");

        logBuffer.add("[" + timeStamp + "]: " + " Connecting to https://kafka-rest-prod02.messagehub.services.us-south.bluemix.net:443/consumers/mytestconsumers/instances/ajay_test_consumer02/topics/offers");

        DownloadTask task = new DownloadTask();
        task.execute("https://kafka-rest-prod02.messagehub.services.us-south.bluemix.net:443/consumers/mytestconsumers/instances/ajay_test_consumer02/topics/offers");


   //     "instance_id": "ajay_test_consumer01",
 //               "base_uri": "https://kafka-rest-prod02.messagehub.services.us-south.bluemix.net:443/consumers/mytestconsumers/instances/ajay_test_consumer01"

        return true;
    }

    public void clearOfferList() {
        offerList.clear();
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

            String result = "";
            URL url;
            HttpsURLConnection urlConnection = null;
            try {

                url = new URL(urls[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("X-Auth-Token", "XAQ1MnwCNySJwPWziVsJXDKKlo8bVrV440D70HfiMfqR9oJQ");
                urlConnection.setRequestProperty("Content-Type", "application/vnd.kafka.binary.v1+json");


                int statusCode = urlConnection.getResponseCode();
                logBuffer.add("[" + timeStamp + "]: statusCode ->" + statusCode);


                if (statusCode == 200) {
                    InputStream it = new BufferedInputStream(urlConnection.getInputStream());
                    InputStreamReader read = new InputStreamReader(it);
                    BufferedReader buff = new BufferedReader(read);
                    StringBuilder dta = new StringBuilder();
                    String chunks;
                    logBuffer.add("[" + timeStamp + "]: statusCode ->" + statusCode);


                    while ((chunks = buff.readLine()) != null) {
                        dta.append(chunks);
//                        System.out.println(dta);
                    }

                    if (dta.length() > 5) {
                        logBuffer.add("[" + timeStamp + "]: Data ->" + dta);

                        String text;
                        try {

                            JSONArray json = (JSONArray) new JSONTokener(dta.toString()).nextValue();
                            JSONObject json2 = json.getJSONObject(0);
                            text = (String) json2.get("value");

                            String base64 = text;
                            // Receiving side
                            byte[] data = Base64.decode(base64, Base64.DEFAULT);
                            String decodedText = new String(data, "UTF-8");

                            logBuffer.add("[" + timeStamp + "]: pullOffers [value] ->" + decodedText);

                            // Add to offer list
                            offerList.add(decodedText);

                            // Shift lift if we more than 4
                           if (offerList.size() > 4) {
                               offerList.remove(0);
                           }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    } else {
                        logBuffer.add("[" + timeStamp + "]: Not enough data, data length = " + dta.length());
                    }


                } else {
                    //Handle else
                    logBuffer.add("[" + timeStamp + "]: statusCode ->" + statusCode);
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