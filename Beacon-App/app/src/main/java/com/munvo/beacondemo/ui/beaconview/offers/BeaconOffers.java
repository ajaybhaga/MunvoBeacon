package com.munvo.beacondemo.ui.beaconview.offers;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.munvo.beacondemo.ui.LocationAnimator;
import com.munvo.beacondemo.ui.beaconview.BeaconView;
import com.munvo.beaconlocate.ble.advertising.AdvertisingPacket;
import com.munvo.beaconlocate.ble.beacon.Beacon;
import com.munvo.beaconlocate.location.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ajay Bhaga on 12.11.18.
 */

public class BeaconOffers extends BeaconView {

    protected Paint zone0Paint;
    protected Paint zone1Paint;
    protected Paint zone2Paint;
    protected int zone = 0;
    protected int beaconNum = 0;

    /*
        Device drawing related variables
     */
    protected ValueAnimator deviceAngleAnimator;
    protected ValueAnimator deviceAccuracyAnimator;
    protected float deviceAccuracyAnimationValue;
    protected float deviceAdvertisingRange;
    protected float deviceAdvertisingRadius;
    protected float deviceStrokeRadius;

    /*
        Beacon drawing related variables
     */
    protected float beaconAccuracyAnimationValue;
    protected float beaconRadius = pixelsPerDip * 8;
    protected float beaconCornerRadius = pixelsPerDip * 2;
    protected float beaconStrokeRadius;
    protected long timeSinceLastAdvertisement;

    /*
        Legend drawing related variables
     */
    protected Paint legendPaint;
    protected int referenceLineCount = 5;
    protected float referenceDistance;
    protected float referenceDistanceStep;
    protected float currentReferenceDistance;
    protected float currentReferenceCanvasUnits;
    protected String referenceText;
    protected float referenceTextWidth;

    /*
        Location mapping related variables
     */
    protected ValueAnimator maximumDistanceAnimator;
    protected double locationDistance;
    protected double locationRadius;
    protected double locationRotationAngle;

    public BeaconOffers(Context context) {
        super(context);
    }

    public BeaconOffers(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BeaconOffers(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BeaconOffers(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void initialize() {
        super.initialize();
        startMaximumDistanceAnimation(100);
        startDeviceAngleAnimation(0);
        legendPaint = new Paint(textPaint);
        legendPaint.setTextSize(pixelsPerDip * 12);
        legendPaint.setStyle(Paint.Style.FILL);
        legendPaint.setColor(Color.BLACK);
        legendPaint.setAlpha(80);

        zone0Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zone0Paint.setStyle(Paint.Style.FILL);
        zone0Paint.setColor(Color.rgb(10,10,10));

        zone1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zone1Paint.setStyle(Paint.Style.FILL);
        zone1Paint.setColor(Color.rgb(160,220,180));

        zone2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zone2Paint.setStyle(Paint.Style.FILL);
        zone2Paint.setColor(Color.rgb(220,220,160));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBeaconAttributeData(canvas);
    }

    @Override
    protected void drawDevice(Canvas canvas) {
        deviceAdvertisingRange = 20; // in meters TODO: get real value based on tx power
        deviceAdvertisingRadius = getCanvasUnitsFromMeters(deviceAdvertisingRange);

        deviceAccuracyAnimationValue = (deviceAccuracyAnimator == null) ? 0 : (float) deviceAccuracyAnimator.getAnimatedValue();
        deviceStrokeRadius = (pixelsPerDip * 10) + (pixelsPerDip * 2 * deviceAccuracyAnimationValue);
//        canvas.drawCircle(canvasCenter.x, canvasCenter.y, deviceStrokeRadius, whiteFillPaint);
//        canvas.drawCircle(canvasCenter.x, canvasCenter.y, deviceStrokeRadius, secondaryStrokePaint);
//        canvas.drawCircle(canvasCenter.x, canvasCenter.y, pixelsPerDip * 8, secondaryFillPaint);
    }

    @Override
    protected void drawBeacons(Canvas canvas) {
        Map<Beacon, PointF> beaconCenterMap = new HashMap<>();
        // draw all backgrounds
        for (Beacon beacon : beacons) {
            PointF beaconCenter = getPointFromLocation(beacon.getLocation(), beacon);
            beaconCenterMap.put(beacon, beaconCenter);
//            drawBeaconBackground(canvas, beacon, beaconCenter);
        }


        boolean mvn1 = false;
        boolean mvn2 = false;
        boolean mvn3 = false;
        boolean mvn4 = false;

        float mvn1Rssi = -99.0f;
        float mvn2Rssi = -99.0f;
        float mvn3Rssi = -99.0f;
        float mvn4Rssi = -99.0f;

        // draw all foregrounds
        for (Beacon beacon : beacons) {

            if ((beacon.getDeviceName().equalsIgnoreCase("mnvn1"))) { // mvn1
                mvn1 = true;
                mvn1Rssi = beacon.getFilteredRssi();
                beaconNum = 1;
            }
            if ((beacon.getDeviceName().equalsIgnoreCase("mnvn2"))) { // mvn2
                mvn2 = true;
                mvn2Rssi = beacon.getFilteredRssi();
                beaconNum = 2;
            }
            if ((beacon.getDeviceName().equalsIgnoreCase("mnvn3"))) { // mvn3
                mvn3 = true;
                mvn3Rssi = beacon.getFilteredRssi();
                beaconNum = 3;
            }
            if ((beacon.getDeviceName().equalsIgnoreCase("mnvn4"))) { // mvn4
                mvn4 = true;
                mvn4Rssi = beacon.getFilteredRssi();
                beaconNum = 4;
            }

            drawBeaconForeground(canvas, beacon, beaconCenterMap.get(beacon));
        }



        if (Math.max(mvn1Rssi, mvn2Rssi) > Math.max(mvn3Rssi, mvn4Rssi)) {
            zone = 2;
        } else {
            zone = 1;
        }

        drawZone(canvas, zone);

        // Zone is detected
        // Send to Kafka
        updateZone();
    }

    protected void updateZone() {

        }

    /**
     * This shouldn't be called, because the created beacon background may overlay existing beacon
     * foregrounds. Use {@link #drawBeacons(Canvas)} instead.
     */
    @Override
    protected void drawBeacon(Canvas canvas, Beacon beacon) {
        PointF beaconCenter = getPointFromLocation(beacon.getLocation(), beacon);
        //drawBeaconBackground(canvas, beacon, beaconCenter);
        //drawBeaconForeground(canvas, beacon, beaconCenter);
    }

    protected void drawZone(Canvas canvas, int zone) {

        Paint zonePaint = zone0Paint;
        RectF rect = new RectF((canvasWidth/2)+100, 0, canvasWidth, canvasHeight);

        switch (zone) {
            case 1:
                zonePaint = zone1Paint;
                break;

            case 2:
                zonePaint = zone2Paint;
                break;
        }

        canvas.drawRoundRect(rect, beaconCornerRadius, beaconCornerRadius, zonePaint);
    }

    protected void drawBeaconBackground(Canvas canvas, Beacon beacon, PointF beaconCenter) {

    }

    protected void drawBeaconForeground(Canvas canvas, Beacon beacon, PointF beaconCenter) {
        AdvertisingPacket latestAdvertisingPacket = beacon.getLatestAdvertisingPacket();
        timeSinceLastAdvertisement = latestAdvertisingPacket != null ? System.currentTimeMillis() - latestAdvertisingPacket.getTimestamp() : 0;

        beaconAccuracyAnimationValue = (deviceAccuracyAnimator == null) ? 0 : (float) deviceAccuracyAnimator.getAnimatedValue();
        beaconAccuracyAnimationValue *= Math.max(0, 1 - (timeSinceLastAdvertisement / 1000));
        beaconStrokeRadius = beaconRadius + (pixelsPerDip * 2) + (pixelsPerDip * 2 * beaconAccuracyAnimationValue);


    }

    protected void drawBeaconAttributeData(Canvas canvas) {

        int beaconYOffset = 0;
        for (Beacon beacon : beacons) {
            PointF beaconCenter = getPointFromLocation(beacon.getLocation(), beacon);
            //beaconCenterMap.put(beacon, beaconCenter);
            //drawBeaconBackground(canvas, beacon, beaconCenter);

        int numKeys = 30;
        String[] key = new String[numKeys];

        int k = 0;
        key[k] = "Name";
        key[++k] = "UUID";
        key[++k] = "MAC Address";
//        key[++k] = "Calibrated Distance"; // = 1 (tested)
//        key[++k] = "Calibrated RSSI"; // = 0 (tested)
        key[++k] = "Distance";
        key[++k] = "Filtered RSSI";
        key[++k] = "Latest Timestamp";
        key[++k] = "RSSI";
//        key[++k] = "Transmission Power"; // = 0 (tested)

        String[] value = new String[numKeys];
        k = 0;
        value[k] = beacon.getDeviceName();

        if (beacon.getUuid() != null) {
            value[++k] = beacon.getUuid().toString();
        } else {
            value[++k] = "No UUID object.";
        }

        value[++k] = beacon.getMacAddress();
//        value[++k] = "" + beacon.getCalibratedDistance();
//        value[++k] = "" + beacon.getCalibratedRssi();
        value[++k] = "" + beacon.getDistance();
        value[++k] = "" + beacon.getFilteredRssi();
        value[++k] = "" + beacon.getLatestTimestamp();
        value[++k] = "" + beacon.getRssi();
//        value[++k] = "" + beacon.getTransmissionPower();

        for (int i = 0; i < 7; i++) {
                // Show
                canvas.drawText(
                        key[i] + ": " + value[i],
                        40,//canvasCenter.x,
                        40+(i*40)+beaconYOffset,
                        legendPaint
                );
            }

            beaconYOffset += (8*40);

        }

        canvas.drawText(
                "Zone: " + zone,
                40,//canvasCenter.x,
                40+(7*40)+beaconYOffset,
                legendPaint);

        canvas.drawText(
                "Closest Beacon: " + beaconNum,
                40,//canvasCenter.x,
                40+(8*40)+beaconYOffset,
                legendPaint);

/*
        if (beacons.isEmpty()) {
            // Show
            canvas.drawText(
                    "No beacons detected.",
                    40,
                    40,
                    legendPaint
            );

        }
*/

    }

    protected PointF getPointFromLocation(Location location) {
        return getPointFromLocation(location, null);
    }

    protected PointF getPointFromLocation(Location location, @Nullable Beacon beacon) {
        if (deviceLocationAnimator == null) {
            return new PointF(canvasCenter.x, canvasCenter.y);
        }
        locationDistance = beacon != null ? beacon.getDistance() : location.getDistanceTo(deviceLocationAnimator.getLocation());
        locationRadius = getCanvasUnitsFromMeters(locationDistance);
        locationRotationAngle = deviceLocationAnimator.getLocation().getAngleTo(location);
        locationRotationAngle = (locationRotationAngle - (float) deviceAngleAnimator.getAnimatedValue()) % 360;
        locationRotationAngle = Math.toRadians(locationRotationAngle) - (Math.PI / 2);
        return new PointF(
                (float) (canvasCenter.x + (locationRadius * Math.cos(locationRotationAngle))),
                (float) (canvasCenter.y + (locationRadius * Math.sin(locationRotationAngle)))
        );
    }

    protected float getCanvasUnitsFromMeters(double meters) {
        return (float) (Math.min(canvasCenter.x, canvasCenter.y) * meters) / (float) maximumDistanceAnimator.getAnimatedValue();
    }

    protected float getMetersFromCanvasUnits(float canvasUnits) {
        return ((float) maximumDistanceAnimator.getAnimatedValue() * canvasUnits) / Math.min(canvasCenter.x, canvasCenter.y);
    }

    public void fitToCurrentLocations() {
        float maximumDistance = 10;
        // TODO: get actual maximum distance
        startMaximumDistanceAnimation(maximumDistance);
    }

    @Override
    public void onDeviceLocationChanged() {
        startDeviceRadiusAnimation();
        super.onDeviceLocationChanged();
    }

    protected void startMaximumDistanceAnimation(float distance) {
        float originValue = distance;
        if (maximumDistanceAnimator != null) {
            originValue = (float) maximumDistanceAnimator.getAnimatedValue();
            maximumDistanceAnimator.cancel();
        }
        maximumDistanceAnimator = ValueAnimator.ofFloat(originValue, distance);
        maximumDistanceAnimator.setDuration(LocationAnimator.ANIMATION_DURATION_LONG);
        maximumDistanceAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                invalidate();
            }
        });
        maximumDistanceAnimator.start();
    }


    protected void startDeviceRadiusAnimation() {
        if (deviceAccuracyAnimator != null && deviceAccuracyAnimator.isRunning()) {
            return;
        }
        deviceAccuracyAnimator = ValueAnimator.ofFloat(0, 1);
        deviceAccuracyAnimator.setDuration(LocationAnimator.ANIMATION_DURATION_LONG);
        deviceAccuracyAnimator.setRepeatCount(1);
        deviceAccuracyAnimator.setRepeatMode(ValueAnimator.REVERSE);
        deviceAccuracyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                invalidate();
            }
        });
        deviceAccuracyAnimator.start();
    }

    public void startDeviceAngleAnimation(float deviceAngle) {
        float originValue = deviceAngle;
        if (deviceAngleAnimator != null) {
            originValue = (float) deviceAngleAnimator.getAnimatedValue();
            deviceAngleAnimator.cancel();
        }
        deviceAngleAnimator = ValueAnimator.ofFloat(originValue, deviceAngle);
        deviceAngleAnimator.setDuration(200);
        deviceAngleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                invalidate();
            }
        });
        deviceAngleAnimator.start();
    }

}
