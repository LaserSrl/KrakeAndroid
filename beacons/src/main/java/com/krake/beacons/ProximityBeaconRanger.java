package com.krake.beacons;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import com.krake.beacons.R;
import com.krake.core.collection.FixedSizeList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Classe per ottenre una notifica quando si è vicini ad un beacon per un tempo configurabile lato App.
 * La classe di appoggia su un inmplementazione di {@link BeaconManager}.
 *
 */
public class ProximityBeaconRanger extends Observable implements Observer {

    private final BeaconManager beaconManager;
    private final SparseArray<FixedSizeList<Integer>> beaconRssi = new SparseArray<>(20);
    private final SparseArray<Long> beaconNearTime = new SparseArray<>(20);

    private final long NumberOfSecondsInNearQueue;
    private final int RssiMediumCount;
    private final double NearDistanceValue;
    private final double SuperBeaconMinimumDistanceDelta;


    //Temp values used to avoid allocation

    private final List<Pair<Double,Beacon>> beaconDistanceNear = new ArrayList<>(20);

    private final ArrayList<Integer> nearBeacons = new ArrayList<>(20);

    public ProximityBeaconRanger(Context context, BeaconManager beaconManager)
    {
        this.beaconManager = beaconManager;
        beaconManager.addObserver(this);
        NumberOfSecondsInNearQueue = context.getResources().getInteger(R.integer.number_of_seconds_in_near_queue);
        RssiMediumCount = context.getResources().getInteger(R.integer.rssi_medium_count);
        NearDistanceValue = context.getResources().getInteger(R.integer.meters_to_be_near);
        SuperBeaconMinimumDistanceDelta = context.getResources().getInteger(R.integer.super_beacon_min_distance_per_ten)/10.0D;
    }

    private double computeDistance(int rssi, int measuredPower) {
        if (rssi == 0) {
            return -1.0D;
        } else {
            double ratio = (double) rssi / (double) measuredPower;
            double rssiCorrection = 0.96D + Math.pow((double) Math.abs(rssi), 3.0D) % 10.0D / 150.0D;
            return ratio <= 1.0D ? Math.pow(ratio, 9.98D) * rssiCorrection : (0.103D + 0.89978D * Math.pow(ratio, 7.71D)) * rssiCorrection;
        }
    }

    public BeaconManager getBeaconManager() {
        return beaconManager;
    }

    @Override
    public void update(Observable observable, Object data) {
        BeaconEvent event = (BeaconEvent) data;
        Region region = ((BeaconEvent) data).getRegion();

        switch (event.getWhat())
        {
            case BeaconEvent.EVENT_ENTER_REGION:
                if (beaconManager.isRegionMain(region))
                    beaconManager.startRangingInRegion(region);

                break;

            case BeaconEvent.EVENT_EXIT_REGION: {
                if (beaconManager.isRegionMain(region)) {
                    beaconManager.stopRangingInRegion(region);
                    beaconRssi.clear();
                    beaconNearTime.clear();
                }
            }
                break;

            case BeaconEvent.EVENT_RANGED_BEACONS:

                processBeacons(event.getBeacons());
                break;

        }
    }

    private void processBeacons(List<Beacon> beacons)
    {
        nearBeacons.ensureCapacity(beacons.size());

        for (int index = 0; index < beacons.size(); index++)
        {
            Beacon b = beacons.get(index);

            FixedSizeList<Integer> values = beaconRssi.get(b.getMinor());
            if(values == null)
            {
                values = new FixedSizeList<>(RssiMediumCount);
                beaconRssi.put(b.getMinor(),values);
            }

            if (!nearBeacons.contains(b.getMinor())) {
                values.add(b.getRssi());
                if (values.remainingCapacity() == 0) {
                    double distance = computeDistance(mediumValues(values), b.getMeasuredPower());
                    if (distance <= NearDistanceValue) {
                        beaconDistanceNear.add(new Pair<>(distance, b));
                        nearBeacons.add(b.getMinor());
                        if (beaconNearTime.get(b.getMinor()) == null) {
                            beaconNearTime.put(b.getMinor(), System.currentTimeMillis() / 1000);
                        }
                    }
                }
            }
        }

        for(int index = 0; index < beaconNearTime.size(); ++index)
        {
            int key = beaconNearTime.keyAt(index);
            if(!nearBeacons.contains(key))
            {
                beaconNearTime.remove(key);
            }
        }

        nearBeacons.clear();

        if(beaconDistanceNear.size() > 0) {

            Collections.sort(beaconDistanceNear, new Comparator<Pair<Double, Beacon>>() {
                @Override
                public int compare(Pair<Double, Beacon> lhs, Pair<Double, Beacon> rhs) {
                    return lhs.first.compareTo(rhs.first);
                }
            });

            Pair<Double,Beacon> superBeacon = beaconDistanceNear.get(0);

            if (beaconDistanceNear.size() > 1) {
                Pair<Double,Beacon> second = beaconDistanceNear.get(1);

                if(Math.abs(superBeacon.first-second.first) < SuperBeaconMinimumDistanceDelta)
                    superBeacon = null;
            }

            beaconDistanceNear.clear();

            if(superBeacon != null)
            {
                Beacon sBeacon = superBeacon.second;
                long timeDiff = System.currentTimeMillis()/1000 - beaconNearTime.get(sBeacon.getMinor());

                if(timeDiff >= NumberOfSecondsInNearQueue)
                {
                    Log.e("Beacon", "Found super beacon");
                    beaconNearTime.clear();
                    setChanged();
                    notifyObservers(sBeacon);
                }
            }
        }

    }

    private int mediumValues(Collection<Integer> values)
    {
        int sum = 0;

        for (Integer i : values)
            sum += i;

        return (sum/values.size());
    }
}
