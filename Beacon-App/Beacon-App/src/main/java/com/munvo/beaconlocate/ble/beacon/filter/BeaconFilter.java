package com.munvo.beaconlocate.ble.beacon.filter;

import com.munvo.beaconlocate.ble.beacon.Beacon;

import java.util.Collection;
import java.util.List;

/**
 * Created by steppschuh on 19.12.17.
 */

public interface BeaconFilter<B extends Beacon> {

    boolean matches(B beacon);

    List<B> getMatches(Collection<B> beacons);

}
