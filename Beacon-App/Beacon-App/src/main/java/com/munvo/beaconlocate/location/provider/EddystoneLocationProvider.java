package com.munvo.beaconlocate.location.provider;

import com.munvo.beaconlocate.ble.beacon.Eddystone;

/**
 * Created by steppschuh on 16.11.17.
 */

public abstract class EddystoneLocationProvider<B extends Eddystone> extends BeaconLocationProvider<B> {

    public EddystoneLocationProvider(B beacon) {
        super(beacon);
    }

}
