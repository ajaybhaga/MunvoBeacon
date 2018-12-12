package com.munvo.beaconlocate.location;

import com.munvo.beaconlocate.location.provider.LocationProvider;

/**
 * Created by steppschuh on 21.11.17.
 */

public interface LocationListener {

    void onLocationUpdated(LocationProvider locationProvider, Location location);

}
