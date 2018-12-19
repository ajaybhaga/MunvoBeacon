package com.munvo.beacondemo.ui.beaconview.log;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.design.widget.CoordinatorLayout;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.munvo.beacondemo.HomeActivity;
import com.munvo.beacondemo.ui.beaconview.BeaconViewFragment;
import com.munvo.beaconlocate.IndoorPositioning;
import com.munvo.beaconlocate.ble.beacon.Beacon;
import com.munvo.beaconlocate.ble.beacon.BeaconUpdateListener;
import com.munvo.beaconlocate.location.Location;
import com.munvo.beaconlocate.location.LocationListener;
import com.munvo.beaconlocate.location.provider.LocationProvider;
import com.munvo.beacondemo.R;

public class BeaconLogFragment extends BeaconViewFragment {

    private BeaconLog beaconLog;
    TextView textView;
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;


    public BeaconLogFragment() {
        super();

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
/*                switch (sensorEvent.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER: {
                        System.arraycopy(sensorEvent.values, 0, accelerometerReading, 0, accelerometerReading.length);
                        break;
                    }
                    case Sensor.TYPE_MAGNETIC_FIELD: {
                        System.arraycopy(sensorEvent.values, 0, magnetometerReading, 0, magnetometerReading.length);
                        break;
                    }
                }
                updateOrientationAngles();


            */
                beaconLog.refresh();
            }


            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

    }

/*

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return super.onCreateView(inflater, container, savedInstanceState);
 /*       View rootView = inflater.inflate(R.layout.fragment_main_dummy,
                container, false);
//        LinearLayout myLayout = (LinearLayout) findViewById(R.id.hl);
        LinearLayout myLayout = (LinearLayout) rootView.findViewById(R.id.hl);


        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        for (int l = 0; l < 4; l++) {
            TextView a = new TextView(this.getContext());
            a.setTextSize(15);
            a.setLayoutParams(lp);
            a.setId(l);
            a.setText((l + 1) + ": something");
            myLayout.addView(a);
        }
*/
//        return rootView;
  //  }


    @CallSuper
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflatedView = super.onCreateView(inflater, container, savedInstanceState);
        beaconLog = inflatedView.findViewById(R.id.beaconLog);
       // beaconOffers.setBeacons(getBeacons());


//        LinearLayout myLayout = (LinearLayout) beaconLog.findViewById(R.id.hl);
/*
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        for (int l = 0; l < 4; l++) {
            TextView a = new TextView(this.getContext());
            a.setTextSize(15);
            a.setLayoutParams(lp);
            a.setId(l);
            a.setText((l + 1) + ": something");
            beaconLog.addView(a);
        }
*/
/*
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        beaconLog.setLayout(new LinearLayout(getContext()));

        textView = new TextView(getContext());
        textView.setTextSize(15);
        textView.setHeight(800);
        textView.setWidth(900);
        textView.setLayoutParams(lp);
        textView.setVisibility(View.VISIBLE);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText(((HomeActivity)getActivity()).getLogBuffer().toString());
        beaconLog.getLayout().addView(textView);
*/

        return inflatedView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onDetach() {
        sensorManager.unregisterListener(sensorEventListener);
        super.onDetach();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_beacon_log;
    }

    @Override
    protected LocationListener createDeviceLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationUpdated(LocationProvider locationProvider, Location location) {
                if (locationProvider == IndoorPositioning.getInstance()) {
                    beaconLog.setDeviceLocation(location);
                    beaconLog.fitToCurrentLocations();
                }
            }
        };
    }

    @Override
    protected BeaconUpdateListener createBeaconUpdateListener() {
        return new BeaconUpdateListener() {
            @Override
            public void onBeaconUpdated(Beacon beacon) {
                beaconLog.setBeacons(getBeacons());
                ((HomeActivity)getActivity()).getZoneDetector().getZone(getBeacons());
                beaconLog.setZoneData(((HomeActivity)getActivity()).getZoneDetector().getZoneData());
                beaconLog.setLogBuffer(((HomeActivity)getActivity()).getLogBuffer());
                ((HomeActivity)getActivity()).getOfferRetriever().getOffers();

//                textView.setText(((HomeActivity)getActivity()).getLogBuffer().toString());

            }
        };
    }
}
