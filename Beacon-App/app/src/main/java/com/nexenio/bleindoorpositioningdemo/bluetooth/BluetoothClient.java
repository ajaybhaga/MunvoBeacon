package com.nexenio.bleindoorpositioningdemo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import com.nexenio.bleindoorpositioning.ble.advertising.AdvertisingPacket;
import com.nexenio.bleindoorpositioning.ble.advertising.IBeaconAdvertisingPacketFactory;
import com.nexenio.bleindoorpositioning.ble.beacon.Beacon;
import com.nexenio.bleindoorpositioning.ble.beacon.BeaconManager;
import com.nexenio.bleindoorpositioning.ble.beacon.IBeacon;
import com.nexenio.bleindoorpositioning.location.Location;
import com.nexenio.bleindoorpositioning.location.provider.IBeaconLocationProvider;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.scan.ScanResult;
import com.polidea.rxandroidble.scan.ScanSettings;

import java.util.Map;

import rx.Observer;
import rx.Subscription;

/**
 * Created by steppschuh on 24.11.17.
 */

public class BluetoothClient {
    final String nrfMACAddress = "66:55:44:33:22:11";
    final String piMACAddress = "B8:27:EB:B8:BE:59";
    final String[] macAddressBeaconFilter = {
                "B8:27:EB:B8:BE:59", // raspberry pi map server
                "11:00:33:33:22:22", // nano nrf24 beacon
                "22:00:33:33:22:22",
                "33:00:33:33:22:22",
                "44:00:33:33:22:22",
                "55:00:33:33:22:22"
    };

    /*

D/BluetoothClient: Processing scan result: [29.641] munvo-beacon-map-svr-01 : B8:27:EB:B8:BE:59 -> -72
D/BluetoothClient: Processing scan result: [29.839] munvo-beacon-map-svr-01 : B8:27:EB:B8:BE:59 -> -71
D/BluetoothClient: Processing scan result: [30.054] munvo-beacon-map-svr-01 : B8:27:EB:B8:BE:59 -> -72
D/BluetoothClient: Processing scan result: [30.155] munvo-beacon-map-svr-01 : B8:27:EB:B8:BE:59 -> -72
D/BluetoothClient: Processing scan result: [29.937] munvo-beacon-map-svr-01 : B8:27:EB:B8:BE:59 -> -72
D/BluetoothClient: Processing scan result: [30.454] munvo-beacon-map-svr-01 : B8:27:EB:B8:BE:59 -> -72
D/BluetoothClient: Processing scan result: [29.734] munvo-beacon-map-svr-01 : B8:27:EB:B8:BE:59 -> -72
D/BluetoothClient: Processing scan result: [30.238] munvo-beacon-map-svr-01 : B8:27:EB:B8:BE:59 -> -72
D/BluetoothClient: Processing scan result: [30.356] munvo-beacon-map-svr-01 : B8:27:EB:B8:BE:59 D/-> -72


Processing scan result: [29.839] mnvn2 : 22:00:33:33:22:22 -> -74
D/BluetoothClient: Processing scan result: [30.271] mnvn1 : 11:00:33:33:22:22 -> -71
D/BluetoothClient: Processing scan result: [30.271] mnvn2 : 22:00:33:33:22:22 -> -71
D/BluetoothClient: Processing scan result: [29.586] mnvn3 : 33:00:33:33:22:22 -> -77
D/BluetoothClient: Processing scan result: [30.288] mnvn4 : 44:00:33:33:22:22 -> -64
     */


    private static final String TAG = BluetoothClient.class.getSimpleName();
    public static final int REQUEST_CODE_ENABLE_BLUETOOTH = 10;

    private static BluetoothClient instance;

    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BeaconManager beaconManager = BeaconManager.getInstance();

    private RxBleClient rxBleClient;
    private Subscription scanningSubscription;

    private static long appStartTime;
    private static String status;

    private BluetoothClient() {
    }

    public static BluetoothClient getInstance() {
        if (instance == null) {
            instance = new BluetoothClient();
        }
        return instance;
    }

    public static void initialize(@NonNull Context context) {
        Log.v(TAG, "Initializing with context: " + context);
        BluetoothClient instance = getInstance();
        instance.rxBleClient = RxBleClient.create(context);
        instance.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        instance.bluetoothAdapter = instance.bluetoothManager.getAdapter();
        if (instance.bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth adapter is not available");
        }

        appStartTime = System.currentTimeMillis();

    }

    public static String getStatus() {

        final BluetoothClient instance = getInstance();
        Map<String, Beacon> beaconMap = instance.beaconManager.getBeaconMap();
        Beacon closestBeacon = instance.beaconManager.getClosestBeacon();

//        status = "Closest Beacon -> " + closestBeacon + ", Detected beacons -> " + beaconMap.toString();
        status = "Detected beacons -> " + beaconMap.size();
        return status;
    }

    public static void startScanning() {
        if (isScanning()) {
            return;
        }

        final BluetoothClient instance = getInstance();
        Log.d(TAG, "Starting to scan for beacons");

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();

        instance.scanningSubscription = instance.rxBleClient.scanBleDevices(scanSettings)
                .subscribe(new Observer<ScanResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Bluetooth scanning error", e);
                    }

                    @Override
                    public void onNext(ScanResult scanResult) {
                        instance.processScanResult(scanResult);
                    }
                });
    }

    public static void stopScanning() {
        if (!isScanning()) {
            return;
        }

        BluetoothClient instance = getInstance();
        Log.d(TAG, "Stopping to scan for beacons");
        status = "Stopping to scan for beacons";
        instance.scanningSubscription.unsubscribe();
    }

    public static boolean isScanning() {
        Subscription subscription = getInstance().scanningSubscription;
        return subscription != null && !subscription.isUnsubscribed();
    }

    public static boolean isBluetoothEnabled() {
        BluetoothClient instance = getInstance();
        return instance.bluetoothAdapter != null && instance.bluetoothAdapter.isEnabled();
    }

    public static void requestBluetoothEnabling(@NonNull Activity activity) {
        Log.d(TAG, "Requesting bluetooth enabling");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BLUETOOTH);
    }

    private void processScanResult(@NonNull ScanResult scanResult) {
        String macAddress = scanResult.getBleDevice().getMacAddress();
        String deviceName = scanResult.getScanRecord().getDeviceName();
        byte[] data = scanResult.getScanRecord().getBytes();

//        for (int i = 0; i < data.length; i++) {
//            Log.d(TAG, "DATA byte[" + i + "]: " + Byte.toString(data[i]));
//


//        if (macAddress.equalsIgnoreCase(nrfMACAddress) || macAddress.equalsIgnoreCase(piMACAddress)) {
        for (int i = 0; i < macAddressBeaconFilter.length; i++) {
            if (macAddress.equalsIgnoreCase(macAddressBeaconFilter[i])) {

                Log.d(TAG, "Processing scan result: [" + ((System.currentTimeMillis() - appStartTime) / 1000.0f) + "] " + scanResult.getScanRecord().getDeviceName() + " : " + macAddress + " -> " + scanResult.getRssi());
                status = "Processing scan result: [" + ((System.currentTimeMillis() - appStartTime) / 1000.0f) + "] " + macAddress + " -> " + scanResult.getRssi();

                AdvertisingPacket advertisingPacket = BeaconManager.processAdvertisingData(macAddress, deviceName, data, scanResult.getRssi());

                if (advertisingPacket != null) {


//                    Beacon beacon = BeaconManager.getBeacon(macAddress, advertisingPacket.getBeaconClass());;
            Log.d(TAG, "Result: [" + ((System.currentTimeMillis()-appStartTime)/1000.0f) + "] " + advertisingPacket.getBeaconClass() + " -> " + advertisingPacket.toString());
/*
                    Beacon beacon = BeaconManager.getBeacon(macAddress, advertisingPacket.getBeaconClass());
                    if (beacon instanceof IBeacon && !beacon.hasLocation()) {
                        beacon.setLocationProvider(createDebuggingLocationProvider((IBeacon) beacon));
                    }

  */
                } else {
                    Log.d(TAG, "Result: [" + ((System.currentTimeMillis() - appStartTime) / 1000.0f) + "] advertisingPacket -> empty.");
                }
            }
        }
    }

    private static IBeaconLocationProvider<IBeacon> createDebuggingLocationProvider(IBeacon iBeacon) {
        final Location beaconLocation = new Location();
        switch (iBeacon.getMinor()) {
            case 1: {
                beaconLocation.setLatitude(52.512437);
                beaconLocation.setLongitude(13.391124);
                beaconLocation.setAltitude(36);
                break;
            }
            case 2: {
                beaconLocation.setLatitude(52.512411788476356);
                beaconLocation.setLongitude(13.390875654442985);
                beaconLocation.setElevation(2.65);
                beaconLocation.setAltitude(36);
                break;
            }
            case 3: {
                beaconLocation.setLatitude(52.51240486636751);
                beaconLocation.setLongitude(13.390770270005437);
                beaconLocation.setElevation(2.65);
                beaconLocation.setAltitude(36);
                break;
            }
            case 4: {
                beaconLocation.setLatitude(52.512426);
                beaconLocation.setLongitude(13.390887);
                beaconLocation.setElevation(2);
                beaconLocation.setAltitude(36);
                break;
            }
            case 5: {
                beaconLocation.setLatitude(52.512347534813834);
                beaconLocation.setLongitude(13.390780437281524);
                beaconLocation.setElevation(2.9);
                beaconLocation.setAltitude(36);
                break;
            }
            case 12: {
                beaconLocation.setLatitude(52.51239708899507);
                beaconLocation.setLongitude(13.390878261276518);
                beaconLocation.setElevation(2.65);
                beaconLocation.setAltitude(36);
                break;
            }
            case 13: {
                beaconLocation.setLatitude(52.51242692608082);
                beaconLocation.setLongitude(13.390872969910035);
                beaconLocation.setElevation(2.65);
                beaconLocation.setAltitude(36);
                break;
            }
            case 14: {
                beaconLocation.setLatitude(52.51240825552749);
                beaconLocation.setLongitude(13.390821867681456);
                beaconLocation.setElevation(2.65);
                beaconLocation.setAltitude(36);
                break;
            }
            case 15: {
                beaconLocation.setLatitude(52.51240194910502);
                beaconLocation.setLongitude(13.390725856632926);
                beaconLocation.setElevation(2.65);
                beaconLocation.setAltitude(36);
                break;
            }
            case 16: {
                beaconLocation.setLatitude(52.512390301005595);
                beaconLocation.setLongitude(13.39077285305359);
                beaconLocation.setElevation(2.65);
                beaconLocation.setAltitude(36);
                break;
            }
            case 17: {
                beaconLocation.setLatitude(52.51241817994876);
                beaconLocation.setLongitude(13.390767908948872);
                beaconLocation.setElevation(2.65);
                beaconLocation.setAltitude(36);
                break;
            }
            case 18: {
                beaconLocation.setLatitude(52.51241494408066);
                beaconLocation.setLongitude(13.390923696709294);
                beaconLocation.setElevation(2.65);
                beaconLocation.setAltitude(36);
                break;
            }
        }
        return new IBeaconLocationProvider<IBeacon>(iBeacon) {
            @Override
            protected void updateLocation() {
                this.location = beaconLocation;
            }

            @Override
            protected boolean canUpdateLocation() {
                return true;
            }
        };
    }

}
