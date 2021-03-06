package com.munvo.beacondemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;


import android.util.Base64;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
//import java.util.jar.Manifest;

import com.munvo.beaconlocate.ble.beacon.Beacon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class ZoneDetector {

    private List<String> logBuffer;
    long lastZoneRefresh;

    List<Integer> zones = new ArrayList<Integer>();
    String json;
    String base64;
    String name;
    //  String json;

    long lastBeacon1Refresh = 0;
    long lastBeacon2Refresh = 0;
    long lastBeacon3Refresh = 0;
    long lastBeacon4Refresh = 0;


    // Topics:
    // zone -> 3 mins last 5 zones
    // context -> app populate text message
    // behaviour -> zone pattern match and context then publish action
    // action -> read action offer

    protected int beaconNum = 0;

    boolean mvn1 = false;
    boolean mvn2 = false;
    boolean mvn3 = false;
    boolean mvn4 = false;

    float mvn1Rssi = -99.0f;
    float mvn2Rssi = -99.0f;
    float mvn3Rssi = -99.0f;
    float mvn4Rssi = -99.0f;

    public ZoneDetector(List<String> logBuffer) {
        this.logBuffer = logBuffer;
    }

    public int getZone(List<Beacon> beacons) {
        int zone = 0;

        // draw all foregrounds
        for (Beacon beacon : beacons) {

            if ((beacon.getDeviceName().equalsIgnoreCase("mnvn1"))) { // mvn1
                mvn1 = true;
                mvn1Rssi = beacon.getFilteredRssi();
                beaconNum = 1;
                lastBeacon1Refresh = System.currentTimeMillis();
            }
            if ((beacon.getDeviceName().equalsIgnoreCase("mnvn2"))) { // mvn2
                mvn2 = true;
                mvn2Rssi = beacon.getFilteredRssi();
                beaconNum = 2;
                lastBeacon2Refresh = System.currentTimeMillis();
            }
            if ((beacon.getDeviceName().equalsIgnoreCase("mnvn3"))) { // mvn3
                mvn3 = true;
                mvn3Rssi = beacon.getFilteredRssi();
                beaconNum = 3;
                lastBeacon3Refresh = System.currentTimeMillis();
            }
            if ((beacon.getDeviceName().equalsIgnoreCase("mnvn4"))) { // mvn4
                mvn4 = true;
                mvn4Rssi = beacon.getFilteredRssi();
                beaconNum = 4;
                lastBeacon4Refresh = System.currentTimeMillis();
            }

        }

        if ((System.currentTimeMillis()-lastBeacon1Refresh) > 2000.0f) {
            mvn1Rssi = -999.00f;
        }
        if ((System.currentTimeMillis()-lastBeacon2Refresh) > 2000.0f) {
            mvn2Rssi = -999.00f;
        }
        if ((System.currentTimeMillis()-lastBeacon3Refresh) > 2000.0f) {
            mvn3Rssi = -999.00f;
        }
        if ((System.currentTimeMillis()-lastBeacon4Refresh) > 2000.0f) {
            mvn4Rssi = -999.00f;
        }

        boolean zoneListUpdate = false;

        if ((mvn1Rssi > mvn2Rssi) && (mvn1Rssi > mvn3Rssi) && (mvn1Rssi > mvn4Rssi)) {
            zone = 1;
        }

        if ((mvn2Rssi > mvn1Rssi) && (mvn2Rssi > mvn3Rssi) && (mvn2Rssi > mvn4Rssi)) {
            zone = 2;
        }

        if ((mvn3Rssi > mvn1Rssi) && (mvn3Rssi > mvn2Rssi) && (mvn3Rssi > mvn4Rssi)) {
            zone = 3;
        }

        if ((mvn4Rssi > mvn1Rssi) && (mvn4Rssi > mvn2Rssi) && (mvn4Rssi > mvn3Rssi)) {
            zone = 4;
        }

        /*
        if (Math.max(mvn1Rssi, mvn2Rssi) > Math.max(mvn3Rssi, mvn4Rssi)) {
            zone = 2;
        } else {
            zone = 1;
        }*/
        zoneListUpdate = true;

        if (zones.isEmpty()) {
            zones.add(zone);
            zoneListUpdate = true;
            lastZoneRefresh = System.currentTimeMillis();
        }

        // Last element do not match current zone
        if (zones.get(zones.size()-1) != zone) {
            zones.add(zone);
            zoneListUpdate = true;
            lastZoneRefresh = System.currentTimeMillis();
        }

        // Maximum series of 4 for 30 seconds x 4 = 2 minutes retention of zone state
        if (((System.currentTimeMillis()-lastZoneRefresh) > 30000.0f) || (zones.size() > 4)) {
            logBuffer.remove(0);
            zones.remove(0);
            lastZoneRefresh = System.currentTimeMillis();
        }


        String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

        if (zoneListUpdate) {

            if (getZoneData().size() > 2) {
                logBuffer.add("[" + timeStamp + "] Publishing Zone List Update ->" + getZoneSeries());
                publishZoneSeries(321);
            }
        }

        return zone;
    }

    public void clear() {
        zones.clear();
    }

    public List<Integer> getZoneData() {
        return zones;
    }


    public String getZoneSeries() {

        String zoneSeries = "[";
        for (int i = 0; i < zones.size(); i++) {
            zoneSeries += String.valueOf(zones.get(i));

            if (i != zones.size()-1)
                zoneSeries += ",";
        }
        zoneSeries += "]";

        return zoneSeries;
    }

    public void publishZoneSeries(int custId) {

        if (((System.currentTimeMillis()-lastZoneRefresh) > 5000.0f)) {

            String zoneSeries = "[";
            for (int i = 0; i < zones.size(); i++) {
                zoneSeries += String.valueOf(zones.get(i));

                if (i != zones.size() - 1)
                    zoneSeries += ",";
            }
            zoneSeries += "]";

            String full = String.valueOf(custId) + "," + zoneSeries;


            System.out.println("Publishing -> " + full);

            byte[] data = new byte[0];
            try {
                data = full.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            base64 = "\"" + Base64.encodeToString(data, Base64.DEFAULT).trim();
//        Log.d("This is the FULL  ", base64);

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

//        Log.i("Json ", json);

            DownloadTask task = new DownloadTask();
            task.execute("https://kafka-rest-prod02.messagehub.services.us-south.bluemix.net:443/topics/zones");
        } else {

            System.out.println("SKIPPING PUBLISH - TOO FAST SUBMISSION.");

        }
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
            } catch (FileNotFoundException e) {
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
