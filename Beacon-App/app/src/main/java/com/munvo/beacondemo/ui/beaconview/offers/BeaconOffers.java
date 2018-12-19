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

import com.munvo.beacondemo.HomeActivity;
import com.munvo.beacondemo.ZoneDetector;
import com.munvo.beacondemo.ui.LocationAnimator;
import com.munvo.beacondemo.ui.beaconview.BeaconView;
import com.munvo.beaconlocate.ble.advertising.AdvertisingPacket;
import com.munvo.beaconlocate.ble.beacon.Beacon;
import com.munvo.beaconlocate.location.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ajay Bhaga on 12.11.18.
 */

public class BeaconOffers extends BeaconView {

    protected Paint zone0Paint;
    protected Paint zone1Paint;
    protected Paint zone2Paint;
    protected Paint zone3Paint;
    protected Paint zone4Paint;

    protected Paint offer1Paint;
    protected Paint offer2Paint;
    protected Paint offer3Paint;
    protected Paint offer4Paint;


    protected Paint zoneTextPaint;
    protected Paint offerTextPaint;

    protected int zone = 0;
    protected String offer = "None";
    protected List<Integer> zoneSeries = new ArrayList<Integer>(5);
    protected List<String> logBuffer;
    protected List<String> offerSeries = new ArrayList<String>(5);

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

    public void setZoneData(List<Integer> zoneSeries) {
        this.zoneSeries = zoneSeries;
        zone = zoneSeries.get(zoneSeries.size()-1);
        refresh();
    }


    public void setOfferData(List<String> offerSeries) {
        this.offerSeries = offerSeries;

        if (!offerSeries.isEmpty()) {
            offer = offerSeries.get(offerSeries.size() - 1);
        }
        refresh();
    }


    public void setLogBuffer(List<String> logBuffer) {
        this.logBuffer = logBuffer;
        refresh();
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
        legendPaint.setAlpha(100);

        zoneTextPaint = new Paint(textPaint);
        zoneTextPaint.setTextSize(pixelsPerDip * 48);
        zoneTextPaint.setStyle(Paint.Style.FILL);
        zoneTextPaint.setColor(Color.BLACK);
        zoneTextPaint.setAlpha(90);

        offerTextPaint = new Paint(textPaint);
        offerTextPaint.setTextSize(pixelsPerDip * 10);
        offerTextPaint.setStyle(Paint.Style.FILL);
        offerTextPaint.setColor(Color.BLACK);
        offerTextPaint.setAlpha(95);

        zone0Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zone0Paint.setStyle(Paint.Style.FILL);
        zone0Paint.setColor(Color.rgb(90,90,90));

        zone1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zone1Paint.setStyle(Paint.Style.FILL);
        zone1Paint.setColor(Color.rgb(160,220,180));

        zone2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zone2Paint.setStyle(Paint.Style.FILL);
        zone2Paint.setColor(Color.rgb(220,220,160));

        zone3Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zone3Paint.setStyle(Paint.Style.FILL);
        zone3Paint.setColor(Color.rgb(120,140,240));

        zone4Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zone4Paint.setStyle(Paint.Style.FILL);
        zone4Paint.setColor(Color.rgb(20,240,120));


        offer1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        offer1Paint.setStyle(Paint.Style.FILL);
        offer1Paint.setColor(Color.rgb(255,0,0));

        offer2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        offer2Paint.setStyle(Paint.Style.FILL);
        offer2Paint.setColor(Color.rgb(0,255,0));

        offer3Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        offer3Paint.setStyle(Paint.Style.FILL);
        offer3Paint.setColor(Color.rgb(0,0,255));

        offer4Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        offer4Paint.setStyle(Paint.Style.FILL);
        offer4Paint.setColor(Color.rgb(255,255,255));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawZone(canvas);
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

    protected void drawBeacons(Canvas canvas) {
        Map<Beacon, PointF> beaconCenterMap = new HashMap<>();
        // draw all backgrounds
        for (Beacon beacon : beacons) {
            PointF beaconCenter = getPointFromLocation(beacon.getLocation(), beacon);
            beaconCenterMap.put(beacon, beaconCenter);
//            drawBeaconBackground(canvas, beacon, beaconCenter);
            drawBeaconForeground(canvas, beacon, beaconCenterMap.get(beacon));
        }


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

    protected void drawZone(Canvas canvas) {

        Paint zonePaint = zone0Paint;
        RectF rect = new RectF(0, 0, canvasWidth, canvasHeight);

        switch (zone) {
            case 1:
                zonePaint = zone1Paint;
                break;

            case 2:
                zonePaint = zone2Paint;
                break;

            case 3:
                zonePaint = zone3Paint;
                break;

            case 4:
                zonePaint = zone4Paint;
                break;

        }

        canvas.drawRoundRect(rect, beaconCornerRadius, beaconCornerRadius, zonePaint);


        Paint offerPaint = zone0Paint;
        int height = 0;

        String offerData = "None";
        for (int h = 1; h < 5; h++) {
            if (offerSeries.size() >= h) {
                offerData = offerSeries.get(h - 1);
            }

            switch (h) {
                case 1:
                    offerPaint = offer1Paint;
                    break;

                case 2:
                    offerPaint = offer2Paint;
                    break;

                case 3:
                    offerPaint = offer3Paint;
                    break;

                case 4:
                    offerPaint = offer4Paint;
                    break;

            }

            height = h * 200;
            rect = new RectF(canvasWidth - 400, height, canvasWidth, canvasHeight);

            canvas.drawRoundRect(rect, beaconCornerRadius, beaconCornerRadius, offerPaint);

            String[] token = offerData.split("\\|");
            String custId = "-";
            String offerName = "-";
            String timestamp = "-";

            if (token.length > 2) {
                custId = token[0];
                offerName = token[1];
                timestamp = token[2];

                canvas.drawText(
                        custId,
                        canvasWidth - 380, height + 40,
                        offerTextPaint);
                canvas.drawText(
                        offerName,
                        canvasWidth - 380, height + 60,
                        offerTextPaint);
                canvas.drawText(
                        timestamp,
                        canvasWidth - 380, height + 80,
                        offerTextPaint);

            }
        }

        canvas.drawText(
                "ZONE " + zone,
                (canvasWidth/2)-180,
                canvasHeight-40,
                zoneTextPaint);

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
//        key[++k] = "UUID";
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
        /*
        if (beacon.getUuid() != null) {
            value[++k] = beacon.getUuid().toString();
        } else {
            value[++k] = "No UUID object.";
        }*/

        value[++k] = beacon.getMacAddress();
//        value[++k] = "" + beacon.getCalibratedDistance();
//        value[++k] = "" + beacon.getCalibratedRssi();
        value[++k] = "" + beacon.getDistance();
        value[++k] = "" + beacon.getFilteredRssi();
        value[++k] = "" + beacon.getLatestTimestamp();
        value[++k] = "" + beacon.getRssi();
//        value[++k] = "" + beacon.getTransmissionPower();

        for (int i = 0; i < 6; i++) {
                // Show
                canvas.drawText(
                        key[i] + ": " + value[i],
                        20,//canvasCenter.x,
                        80+(i*40)+beaconYOffset,
                        legendPaint
                );
            }

            beaconYOffset += (8*40);

        }

        /* Global attributes */

        canvas.drawText(
                "Zone Series: " + zoneSeries,
                40,//canvasCenter.x,
                40+(7*40)+beaconYOffset,
                legendPaint);

        /*
        canvas.drawText(
                "Closest Beacon: " + beaconNum,
                40,//canvasCenter.x,
                0+(1*40)+beaconYOffset,
                legendPaint);
*/
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

    protected void refresh() {
        invalidate();
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
