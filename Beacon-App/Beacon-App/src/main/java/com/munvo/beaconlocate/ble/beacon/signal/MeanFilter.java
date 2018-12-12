package com.munvo.beaconlocate.ble.beacon.signal;

import com.munvo.beaconlocate.ble.advertising.AdvertisingPacket;
import com.munvo.beaconlocate.ble.advertising.AdvertisingPacketUtil;
import com.munvo.beaconlocate.ble.beacon.Beacon;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by leon on 03.01.18.
 */

public class MeanFilter extends WindowFilter {

    public MeanFilter() {
    }

    public MeanFilter(long duration, TimeUnit timeUnit) {
        super(duration, timeUnit);
    }

    public MeanFilter(long maximumTimestamp) {
        super(maximumTimestamp);
    }

    public MeanFilter(long duration, TimeUnit timeUnit, long maximumTimestamp) {
        super(duration, timeUnit, maximumTimestamp);
    }

    @Override
    public float filter(Beacon beacon) {
        List<AdvertisingPacket> advertisingPackets = getRecentAdvertisingPackets(beacon);
        int[] rssiArray = AdvertisingPacketUtil.getRssisFromAdvertisingPackets(advertisingPackets);
        return AdvertisingPacketUtil.calculateMean(rssiArray);
    }

}
