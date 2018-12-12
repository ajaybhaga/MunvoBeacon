package com.munvo.beaconlocate.ble.beacon;

import com.munvo.beaconlocate.ble.advertising.EddystoneAdvertisingPacket;
import com.munvo.beaconlocate.location.provider.BeaconLocationProvider;
import com.munvo.beaconlocate.location.provider.EddystoneLocationProvider;

/**
 * Created by steppschuh on 15.11.17.
 */

public class Eddystone<P extends EddystoneAdvertisingPacket> extends Beacon<P> {

    public static final int CALIBRATION_DISTANCE_DEFAULT = 0;

    public Eddystone() {
        this.calibratedDistance = CALIBRATION_DISTANCE_DEFAULT;
    }

    @Override
    public BeaconLocationProvider<Eddystone<P>> createLocationProvider() {
        return new EddystoneLocationProvider<Eddystone<P>>(this) {
            @Override
            protected void updateLocation() {
                // nope
            }

            @Override
            protected boolean canUpdateLocation() {
                return false;
            }
        };
    }

}
