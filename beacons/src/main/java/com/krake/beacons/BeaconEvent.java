package com.krake.beacons;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by joel on 21/04/16.
 */
public class BeaconEvent {
    static final public int EVENT_ENTER_REGION = 9871;
    static final public int EVENT_EXIT_REGION = 9872;
    static final public int EVENT_RANGED_BEACONS = 9873;

    private final @NonNull Region region;
    private final
    @Event
    int what;
    private final boolean isMainRegion;
    private final @Nullable
    List<Beacon> beacons;

    private BeaconEvent(@NonNull Region region, @Event int what, boolean isMainRegion, @Nullable List<Beacon> beacons) {

        this.region = region;
        this.what = what;
        this.isMainRegion = isMainRegion;
        this.beacons = beacons;
    }

    public static BeaconEvent createEnterRegion(Region region, boolean isMainRegion) {
        return new BeaconEvent(region, EVENT_ENTER_REGION, isMainRegion, null);
    }

    public static BeaconEvent createExitRegion(Region region, boolean isMainRegion) {
        return new BeaconEvent(region, EVENT_EXIT_REGION, isMainRegion, null);
    }

    public static BeaconEvent createRangeBeacon(Region region, List<Beacon> beacons, boolean isMainRegion) {
        return new BeaconEvent(region, EVENT_RANGED_BEACONS, isMainRegion, beacons);
    }

    @NonNull
    public Region getRegion() {
        return region;
    }

    public
    @Event
    int getWhat() {
        return what;
    }

    @Nullable
    public List<Beacon> getBeacons() {
        return beacons;
    }

    @IntDef({EVENT_ENTER_REGION, EVENT_EXIT_REGION, EVENT_RANGED_BEACONS})
    //Tell the compiler not to store annotation data in the .class file
    @Retention(RetentionPolicy.SOURCE)

            //Declare the BeaconEvent annotation
    @interface Event {
    }
}
