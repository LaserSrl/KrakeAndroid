package com.krake.core.extension

import android.content.Context
import android.os.SystemClock
import com.krake.core.net.SntpClient
import java.util.*

/**
 * Created by joel on 27/04/17.
 */

fun Date.isInSameDay(other: Date): Boolean {
    val calendar = Calendar.getInstance()
    val otherCalendar = Calendar.getInstance()

    calendar.time = this
    otherCalendar.time = other

    return calendar.get(Calendar.DAY_OF_YEAR) == otherCalendar.get(Calendar.DAY_OF_YEAR) &&
            calendar.get(Calendar.YEAR) == otherCalendar.get(Calendar.YEAR)
}


object DateNTP {
    private var ntpTime: Long = 0
    private var ntpSystemReference: Long = 0
    private var ntpClientTimeLoaded = false

    fun isNtpClientTimeLoaded() : Boolean
    {
        return ntpClientTimeLoaded;
    }

    fun loadNtpTime(ntpServers: Array<String>)
    {
        val client = SntpClient()

        for (server in ntpServers)
        {
            if (client.requestTime(server, 10000))
            {
                ntpTime = client.ntpTime
                ntpSystemReference = client.ntpTimeReference
                ntpClientTimeLoaded = true
                break
            }
        }
    }

    fun ntpNowInMillis() : Long
    {
        return (if (ntpClientTimeLoaded) ntpTime + SystemClock.elapsedRealtime() - ntpSystemReference else Date().time)
    }
}