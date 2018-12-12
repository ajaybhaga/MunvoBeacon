package com.munvo.beaconlocate.location.provider;

import com.munvo.beaconlocate.ble.beacon.IBeacon;

/**
 * Created by steppschuh on 16.11.17.
 */

public abstract class IBeaconLocationProvider<B extends IBeacon> extends BeaconLocationProvider<B> {

    public IBeaconLocationProvider(B beacon) {
        super(beacon);
    }

}
