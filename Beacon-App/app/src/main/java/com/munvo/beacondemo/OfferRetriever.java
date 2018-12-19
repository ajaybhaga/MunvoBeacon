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

//import java.util.jar.Manifest;


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

        logBuffer.add("[" + timeStamp + "]: " + " Connecting to https://kafka-rest-prod02.messagehub.services.us-south.bluemix.net:443/consumers/mytestconsumers/instances/ajay_test_consumer01/topics/offers");

        DownloadTask task = new DownloadTask();
        task.execute("https://kafka-rest-prod02.messagehub.services.us-south.bluemix.net:443/consumers/mytestconsumers/instances/ajay_test_consumer01/topics/offers");


   //     "instance_id": "ajay_test_consumer01",
 //               "base_uri": "https://kafka-rest-prod02.messagehub.services.us-south.bluemix.net:443/consumers/mytestconsumers/instances/ajay_test_consumer01"

/*
        try {

            URL url = new URL("https://kafka-rest-prod02.messagehub.services.us-south.bluemix.net:443/consumers/mytestconsumers/instances/rest-consumer-kafka-rest-consume.eed03688-02c0-11e9-8e0d-6204921da560-86960c03-2198-43cc-9849-7fd5fc875372/topics/offers");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            int statusCode = urlConnection.getResponseCode();

            if (statusCode == 200) {
                InputStream it = new BufferedInputStream(urlConnection.getInputStream());
                InputStreamReader read = new InputStreamReader(it);
                BufferedReader buff = new BufferedReader(read);
                StringBuilder dta = new StringBuilder();
                String chunks;
                logBuffer.add("[" + timeStamp + "]: statusCode ->" + statusCode);


                while ((chunks = buff.readLine()) != null) {
                    dta.append(chunks);
                    System.out.println(dta);
                    logBuffer.add("[" + timeStamp + "]: Data ->" + dta);
                }
            } else {
                //Handle else
                logBuffer.add("[" + timeStamp + "]: statusCode ->" + statusCode);
            }
        } catch (Exception e) {
            //   System.out.println(e.fillInStackTrace());

            String message = getStackTrace(e);

            logBuffer.add("[" + timeStamp + "]: Exception ->" + message);
        }*/

        return true;
    }


    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }


    /*

json =    {"instance_id":"rest-consumer-kafka-rest-consume.eed03688-02c0-11e9-8e0d-6204921da560-86960c03-2198-43cc-9849-7fd5fc875372",
    "base_uri":"https://kafka-rest-prod02.messagehub.services.us-south.bluemix.net:443/consumers/mytestconsumers/instances/rest-consumer-kafka-rest-consume.eed03688-02c0-11e9-8e0d-6204921da560-86960c03-2198-43cc-9849-7fd5fc875372"}

     */




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

//                        JSONObject jsonObject = new JSONObject();
                            //                      JSONObject myResponse = jsonObject.getJSONObject("data");
                            //                    JSONArray valueResponse = (JSONArray) myResponse.get("value");
//

                            String base64 = text;
                            // Receiving side
                            byte[] data = Base64.decode(base64, Base64.DEFAULT);
                            String decodedText = new String(data, "UTF-8");


                            logBuffer.add("[" + timeStamp + "]: myResponse [value] ->" + decodedText);



                            offerList.add(decodedText);
                           if (offerList.size() > 5) {
                               offerList.remove(0);
                           }

                            //                }

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


//
//
// DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
//                wr.writeBytes(json);
 //               logBuffer.add(json);                int statusCode = urlConnection.getResponseCode();

 //               wr.flush();
  //              wr.close();

/*
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
*/
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
