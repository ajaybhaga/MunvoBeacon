package com.munvo.beacondemo.ui.beaconview.log;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.munvo.beacondemo.HomeActivity;
import com.munvo.beacondemo.ZoneDetector;
import com.munvo.beacondemo.ui.LocationAnimator;
import com.munvo.beacondemo.ui.beaconview.BeaconView;
import com.munvo.beaconlocate.ble.advertising.AdvertisingPacket;
import com.munvo.beaconlocate.ble.beacon.Beacon;
import com.munvo.beaconlocate.location.Location;
import com.munvo.beaconlocate.location.distance.DistanceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Created by steppschuh on 16.11.17.
 */

public class BeaconLog extends BeaconView {

    protected int zone = 0;
    protected List<Integer> zoneSeries = new ArrayList<Integer>(5);
    protected List<String> logBuffer;

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

    public BeaconLog(Context context) {
        super(context);
    }

    public BeaconLog(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BeaconLog(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BeaconLog(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setZoneData(List<Integer> zoneSeries) {
        this.zoneSeries = zoneSeries;
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
        legendPaint.setAlpha(50);



        Random r = new Random();
    /*
        TEST DATA

    for (int i = 0; i < 255; i++) {
            long rand = r.nextLong();
            logBuffer.append(rand);
            logBuffer.append("|");
        }*/
    }


    protected void drawLogData(Canvas canvas) {
        if ((logBuffer != null) && !logBuffer.isEmpty()) {

            int numKeys = 30;
            String[] key = new String[numKeys];
            String[] value = new String[numKeys];

            for (int k = 0; k < Math.min(logBuffer.size(), numKeys); k++) {
                key[k] = "[" + k + "]";
                value[k] = logBuffer.get(k);
            }
            for (int i = 0; i < Math.min(logBuffer.size(), numKeys); i++) {
                // Show
                canvas.drawText(
                        key[i] + " -> " + value[i],
                        20,//canvasCenter.x,
                        80 + (i * 40),
                        legendPaint
                );
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawLogData(canvas);
    }

    @Override
    protected void drawDevice(Canvas canvas) {

//        canvas.drawCircle(canvasCenter.x, canvasCenter.y, deviceStrokeRadius, whiteFillPaint);
    }

    @Override
    protected void drawBeacons(Canvas canvas) {
        Map<Beacon, PointF> beaconCenterMap = new HashMap<>();
        // draw all backgrounds
        for (Beacon beacon : beacons) {
            PointF beaconCenter = getPointFromLocation(beacon.getLocation(), beacon);
            beaconCenterMap.put(beacon, beaconCenter);
            drawBeaconBackground(canvas, beacon, beaconCenter);
        }
    }

    /**
     * This shouldn't be called, because the created beacon background may overlay existing beacon
     * foregrounds. Use {@link #drawBeacons(Canvas)} instead.
     */
    @Override
    protected void drawBeacon(Canvas canvas, Beacon beacon) {
        PointF beaconCenter = getPointFromLocation(beacon.getLocation(), beacon);
        drawBeaconBackground(canvas, beacon, beaconCenter);
    }

    protected void drawBeaconBackground(Canvas canvas, Beacon beacon, PointF beaconCenter) {

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

    protected void refresh() {
        invalidate();
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
