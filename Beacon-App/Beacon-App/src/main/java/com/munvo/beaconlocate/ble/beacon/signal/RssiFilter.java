package com.munvo.beaconlocate.ble.beacon.signal;

import com.munvo.beaconlocate.ble.beacon.Beacon;

/**
 * Created by leon on 03.01.18.
 */

public interface RssiFilter {

    float filter(Beacon beacon);

}
