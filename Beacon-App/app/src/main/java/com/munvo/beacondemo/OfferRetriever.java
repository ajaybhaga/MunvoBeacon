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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

//import java.util.jar.Manifest;


public class OfferRetriever {

    private List<String> logBuffer;
    long lastOfferRefresh = 0;
    String json;
    String base64;

    public OfferRetriever(List<String> logBuffer) {
        this.logBuffer = logBuffer;
    }

    public int getOffers() {

        // Every 5 seconds
        if ((System.currentTimeMillis()-lastOfferRefresh) > 5000.0f) {
            if (pullOffers())
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

        return 0;
    }

    public boolean pullOffers() {


//        String full = String.valueOf(custId) + "," + zoneSeries;

  //      System.out.println("Publishing -> " + full);

        /*
        byte[] data = new byte[0];
        try {
            data = full.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        base64 = "\"" + Base64.encodeToString(data, Base64.DEFAULT).trim();
        Log.d("This is the FULL  ", base64);

        json = " {\n" +
                "  \"records\": [\n" +
                "    {\n" +
                "      \"value\" :" + base64 + "\"" +
                "    }\n" +
                "  ]\n" +
                "}\n";


/*
                    '
                    {
                        "records": [
                        {
                            "value": "A base 64 encoded value string"
                        }
                        ]
                    }
                    '*/
/*
        Log.i("Json ", json);
*/


        String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

        logBuffer.add("[" + timeStamp + "] Retrieving any offers.");

        // Change to
//        DownloadTask task = new DownloadTask();
 //       task.execute("https://kafka-rest-prod02.messagehub.services.us-south.bluemix.net:443/topics/offers");
        return true;
    }

    /*

    {"instance_id":"rest-consumer-kafka-rest-consume.eed03688-02c0-11e9-8e0d-6204921da560-86960c03-2198-43cc-9849-7fd5fc875372",
    "base_uri":"https://kafka-rest-prod02.messagehub.services.us-south.bluemix.net:443/consumers/mytestconsumers/instances/rest-consumer-kafka-rest-consume.eed03688-02c0-11e9-8e0d-6204921da560-86960c03-2198-43cc-9849-7fd5fc875372"}

     */

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
//                wr.writeBytes(json);
 //               logBuffer.add(json);
 //               wr.flush();
  //              wr.close();

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
