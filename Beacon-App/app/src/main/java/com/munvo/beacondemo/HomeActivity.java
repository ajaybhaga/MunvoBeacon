package com.munvo.beacondemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.munvo.beacondemo.bluetooth.BluetoothClient;
import com.munvo.beacondemo.location.AndroidLocationProvider;
import com.munvo.beacondemo.ui.beaconview.chart.BeaconChartFragment;
import com.munvo.beacondemo.ui.beaconview.map.BeaconMapFragment;
import com.munvo.beacondemo.ui.beaconview.log.BeaconLogFragment;
import com.munvo.beacondemo.ui.beaconview.offers.BeaconOffersFragment;
import com.munvo.beacondemo.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private CoordinatorLayout coordinatorLayout;
    private BottomNavigationView bottomNavigationView;
    private TextView textStatus;

    // Data Buffer
    private List<String> logBuffer;
    long lastLogRefresh = 0;
    protected ZoneDetector zoneDetector;
    protected OfferRetriever offerRetriever;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // setup UI
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_offers);

        // Set default text
        textStatus = (TextView)findViewById(R.id.textStatus);
        textStatus.setText("-");

        logBuffer = new ArrayList<String>(100);
        zoneDetector = new ZoneDetector(logBuffer);
        offerRetriever = new OfferRetriever(logBuffer);

        // setup location
        AndroidLocationProvider.initialize(this);

        // setup bluetooth
        BluetoothClient.initialize(this);

        final Button clearSeriesButton = findViewById(R.id.clearSeriesButton);
        clearSeriesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                getZoneDetector().clear();
                String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
                logBuffer.add("[" + timeStamp + "] Cleared zone series ->" + getZoneDetector().getZoneSeries());
            }
        });

        final Button clearLogsButton = findViewById(R.id.clearLogsButton);
        clearLogsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                logBuffer.clear();
            }
        });

    }

    public List<String> getLogBuffer() {
        return logBuffer;
    }
    public ZoneDetector getZoneDetector() { return zoneDetector; }
    public OfferRetriever getOfferRetriever() { return offerRetriever; }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter:
                Log.w(TAG, "BeaconFilter");
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        if (textStatus != null) {
            textStatus.setText(BluetoothClient.getStatus());
        }

        switch (item.getItemId()) {

            case R.id.navigation_offers: {
                selectedFragment = new BeaconOffersFragment();
                break;
            }

            case R.id.navigation_map: {
                selectedFragment = new BeaconMapFragment();
                break;
            }

            case R.id.navigation_log: {
                selectedFragment = new BeaconLogFragment();
                break;
            }
            case R.id.navigation_chart: {
                selectedFragment = new BeaconChartFragment();
                break;
            }
        }
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, selectedFragment).commit();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // observe location
        if (!AndroidLocationProvider.hasLocationPermission(this)) {
            AndroidLocationProvider.requestLocationPermission(this);
        } else if (!AndroidLocationProvider.isLocationEnabled(this)) {
            requestLocationServices();
        }
        AndroidLocationProvider.startRequestingLocationUpdates();
        AndroidLocationProvider.requestLastKnownLocation();

        // observe bluetooth
        if (!BluetoothClient.isBluetoothEnabled()) {
            requestBluetooth();
        }
        BluetoothClient.startScanning();
        textStatus.setText(BluetoothClient.getStatus());

        logBuffer.add(System.currentTimeMillis()+ ": Resumed.");
    }

    @Override
    protected void onPause() {
        // stop observing location
        AndroidLocationProvider.stopRequestingLocationUpdates();

        // stop observing bluetooth
        BluetoothClient.stopScanning();
        textStatus.setText(BluetoothClient.getStatus());
        logBuffer.add(System.currentTimeMillis()+ ": Paused.");

        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AndroidLocationProvider.REQUEST_CODE_LOCATION_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Location permission granted");
                    textStatus.setText("Location permission granted");

                    AndroidLocationProvider.startRequestingLocationUpdates();
                } else {
                    Log.d(TAG, "Location permission not granted. Wut?");
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BluetoothClient.REQUEST_CODE_ENABLE_BLUETOOTH: {
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Bluetooth enabled, starting to scan");
                    textStatus.setText("Bluetooth enabled, starting to scan");

                    BluetoothClient.startScanning();
                } else {
                    Log.d(TAG, "Bluetooth not enabled, invoking new request");
                    textStatus.setText("Bluetooth not enabled, invoking new request");

                    BluetoothClient.requestBluetoothEnabling(this);
                }
                break;
            }
        }
        textStatus.setText(BluetoothClient.getStatus());
    }

    private void requestLocationServices() {
        Snackbar snackbar = Snackbar.make(
                coordinatorLayout,
                R.string.error_location_disabled,
                Snackbar.LENGTH_INDEFINITE
        );
        snackbar.setAction(R.string.action_enable, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AndroidLocationProvider.requestLocationEnabling(HomeActivity.this);
            }
        });
        snackbar.show();
    }

    private void requestBluetooth() {
        Snackbar snackbar = Snackbar.make(
                coordinatorLayout,
                R.string.error_bluetooth_disabled,
                Snackbar.LENGTH_INDEFINITE
        );
        snackbar.setAction(R.string.action_enable, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothClient.requestBluetoothEnabling(HomeActivity.this);
            }
        });
        snackbar.show();
    }

}
