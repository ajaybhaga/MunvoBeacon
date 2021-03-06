package com.munvo.beacondemo.ui.beaconview.offers;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.munvo.beacondemo.HomeActivity;
import com.munvo.beacondemo.ui.beaconview.BeaconViewFragment;
import com.munvo.beaconlocate.IndoorPositioning;
import com.munvo.beaconlocate.ble.beacon.Beacon;
import com.munvo.beaconlocate.ble.beacon.BeaconUpdateListener;
import com.munvo.beaconlocate.location.Location;
import com.munvo.beaconlocate.location.LocationListener;
import com.munvo.beaconlocate.location.provider.LocationProvider;
import com.munvo.beacondemo.R;

import java.util.ArrayList;
import java.util.List;

public class BeaconOffersFragment extends BeaconViewFragment {

    private BeaconOffers beaconOffers;
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    public BeaconOffersFragment() {
        super();
        beaconFilters.add(uuidFilter);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                switch (sensorEvent.sensor.getType()) {
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
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
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
        return R.layout.fragment_beacon_offers;
    }

    @Override
    protected LocationListener createDeviceLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationUpdated(LocationProvider locationProvider, Location location) {
                if (locationProvider == IndoorPositioning.getInstance()) {
                    beaconOffers.setDeviceLocation(location);
                    beaconOffers.fitToCurrentLocations();
                }
            }
        };
    }

    @Override
    protected BeaconUpdateListener createBeaconUpdateListener() {
        return new BeaconUpdateListener() {
            @Override
            public void onBeaconUpdated(Beacon beacon) {
                beaconOffers.setBeacons(getBeacons());
                ((HomeActivity)getActivity()).getZoneDetector().getZone(getBeacons());
                beaconOffers.setZoneData(((HomeActivity)getActivity()).getZoneDetector().getZoneData());
                beaconOffers.setLogBuffer(((HomeActivity)getActivity()).getLogBuffer());
                beaconOffers.setOfferData(((HomeActivity)getActivity()).getOfferRetriever().getOffers());
            }
        };
    }

    @CallSuper
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflatedView = super.onCreateView(inflater, container, savedInstanceState);
        beaconOffers = inflatedView.findViewById(R.id.beaconOffers);
        beaconOffers.setBeacons(getBeacons());
        return inflatedView;
    }

    private void updateOrientationAngles() {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        beaconOffers.startDeviceAngleAnimation((float) Math.toDegrees(orientationAngles[0]));
    }

}
