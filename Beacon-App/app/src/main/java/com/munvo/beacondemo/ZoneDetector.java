package com.munvo.beacondemo;
/*
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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import android.util.Base64;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
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
*/
import javax.net.ssl.HttpsURLConnection;


public class ZoneDetector {
/*
        LocationManager locationManager;
        LocationListener locationListener;
        String json;
        String base64;
        String name;
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    return;
                }

            }

        }
        //  String json;


        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void trackme(View view) throws UnsupportedEncodingException {

            EditText username = (EditText) findViewById(R.id.editText2);
            if (username.getText().toString().trim().length() <= 0) {
                Toast.makeText(this," Name field is blank", Toast.LENGTH_SHORT).show();
            } else {
                name = username.getText().toString();
                // Toast.makeText(this, " Name = =  " + name, Toast.LENGTH_SHORT).show();
                // byte[] data = name.getBytes("UTF-8");
                //  base64 = "\""+ Base64.encodeToString(data, Base64.DEFAULT).trim();
                //Log.d ("This is the name  ", base64);

                locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(android.location.Location location) {
                        String full = name + "," + String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude());

                        byte[] data = new byte[0];
                        try {
                            data = full.getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        base64 = "\""+ Base64.encodeToString(data, Base64.DEFAULT).trim();
                        Log.d ("This is the FULL  ", base64);

                        json = " {\n" +
                                "  \"records\": [\n" +
                                "    {\n" +
                                "      \"value\" :"+base64 +"\""+
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

                        DownloadTask task = new DownloadTask();
                        task.execute("https://kafka-rest-prod02.messagehub.services.us-south.bluemix.net:443/topics/kafka-java-console-sample-topic");
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                };

                //explicitly ask user for permission

                // check if we already have permission
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

                    //ask for permission
                    ActivityCompat.requestPermissions(this,new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }else{
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                }


            }


        }
        @Override
        protected void onCreate(Bundle savedInstanceState) {


            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);




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
                    urlConnection.setRequestProperty("X-Auth-Token","XAQ1MnwCNySJwPWziVsJXDKKlo8bVrV440D70HfiMfqR9oJQ");
                    urlConnection.setRequestProperty("Content-Type", "application/vnd.kafka.binary.v1+json");

                    DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                    wr.writeBytes(json);
                    wr.flush();
                    wr.close();

                    InputStream in = urlConnection.getInputStream();
                    InputStreamReader reader = new InputStreamReader(in);
                    int data = reader.read();
                    while (data != -1){
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
            protected void onPostExecute(String s){
                super.onPostExecute(s);

                Log.d("Message Sent", s);

            }
        }
*/

    }
