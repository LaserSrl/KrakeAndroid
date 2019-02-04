package com.krake.beacons.altbeacon

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import com.krake.beacons.BeaconEvent
import com.krake.beacons.BeaconManager
import com.krake.beacons.R
import com.krake.beacons.Region
import org.altbeacon.beacon.*
import java.util.*

class AltBeaconManager(context: Context,
                       val mainRegion: org.altbeacon.beacon.Region,
                       vararg regions: org.altbeacon.beacon.Region) :
        Observable(), BeaconManager, BeaconConsumer, MonitorNotifier, RangeNotifier
{
    private val internalManager: org.altbeacon.beacon.BeaconManager
    private val appContext: Context

    companion object {
        const val IBEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
    }

    private val allRegions: ArrayList<org.altbeacon.beacon.Region>
    private val regionsState: MutableMap<String, Int> = mutableMapOf()

    init
    {
        allRegions = ArrayList<org.altbeacon.beacon.Region>(regions.size + 1)
        allRegions.add(mainRegion)
        allRegions.addAll(Arrays.asList(*regions))

        internalManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(context)
        internalManager.beaconParsers.add(BeaconParser().setBeaconLayout(IBEACON_LAYOUT))
        val resources = context.resources
        internalManager.backgroundScanPeriod = resources.getInteger(R.integer.beacon_background_scan_period) * 1000L
        internalManager.backgroundBetweenScanPeriod = resources.getInteger(R.integer.beacon_background_wait_period) * 1000L
        internalManager.foregroundBetweenScanPeriod = resources.getInteger(R.integer.beacon_foreground_scan_period) * 1000L
        internalManager.foregroundBetweenScanPeriod = resources.getInteger(R.integer.beacon_foreground_scan_period) * 1000L
        appContext = context.applicationContext
    }

    //*** BeaconConsumer

    override fun getApplicationContext(): Context
    {
        return appContext
    }

    override fun unbindService(p0: ServiceConnection?)
    {
        appContext.unbindService(p0)
    }

    override fun bindService(p0: Intent?, p1: ServiceConnection?, p2: Int): Boolean
    {
       return appContext.bindService(p0, p1, p2)
    }

    override fun onBeaconServiceConnect()
    {
        if (!isMonitoring)
        {
            isMonitoring = true
            internalManager.addMonitorNotifier(this)
            allRegions.forEach {

                internalManager.startMonitoringBeaconsInRegion(it)
                //internalManager.startRangingBeaconsInRegion(it)
            }
        }
    }

    override fun didDetermineStateForRegion(p0: Int, p1: org.altbeacon.beacon.Region?)
    {
        if (p1 != null && regionsState[p1.uniqueId] != p0)
        {
            if (p0 == MonitorNotifier.INSIDE)
            {
                didEnterRegion(p1)
            }
            else
            {
                didExitRegion(p1)
            }
        }
    }

    override fun didEnterRegion(p0: org.altbeacon.beacon.Region?)
    {
        if (p0 != null && regionsState[p0.uniqueId] != MonitorNotifier.INSIDE)
        {
            regionsState[p0.uniqueId] = MonitorNotifier.INSIDE
            setChanged()
            notifyObservers(BeaconEvent.createEnterRegion(AltBeaconRegion(p0), p0 === mainRegion))
        }
    }

    override fun didExitRegion(p0: org.altbeacon.beacon.Region?)
    {
        if (p0 != null && regionsState[p0.uniqueId] != MonitorNotifier.OUTSIDE)
        {
            regionsState[p0.uniqueId] = MonitorNotifier.OUTSIDE
            setChanged()
            notifyObservers(BeaconEvent.createExitRegion(AltBeaconRegion(p0), p0 === mainRegion))
        }
    }


    //*** BeaconManager

    override var isMonitoring: Boolean = false
        private set

    override fun startRegionMonitoring(context: Context?): Boolean
    {
        if (isMonitoring.not())
            internalManager.bind(this)
        return true
    }

    override fun stopRegionMonitoring()
    {
        allRegions.forEach {
            internalManager.stopMonitoringBeaconsInRegion(it)
            regionsState.remove(it.uniqueId)
        }
        internalManager.removeMonitorNotifier(this)
        internalManager.unbind(this)
        isMonitoring = false
    }

    override fun isRegionMain(region: Region): Boolean
    {
        return region.originalRegion == mainRegion
    }

    override fun startRangingInRegion(region: Region)
    {

        internalManager.startRangingBeaconsInRegion(region.originalRegion as org.altbeacon.beacon.Region)
        internalManager.addRangeNotifier(this)
    }

    override fun stopRangingInRegion(region: Region)
    {
        internalManager.stopRangingBeaconsInRegion(region.originalRegion as org.altbeacon.beacon.Region)
        internalManager.removeRangeNotifier(this)
    }

    override fun didRangeBeaconsInRegion(p0: MutableCollection<Beacon>?, p1: org.altbeacon.beacon.Region?)
    {
        if (p0 != null && p1 != null)
        {
            setChanged()
            notifyObservers(BeaconEvent.createRangeBeacon(AltBeaconRegion(p1), p0.map { AltBeaconBeacon(it) }, p1 === mainRegion))
        }
    }
}