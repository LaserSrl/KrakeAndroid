/**
 * Created by Krake Generator 10.0 Bloody Mary (1906.25.14) on 29/08/2019, 10:23**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithStringIdentifier
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class StopTime : RealmObject() /*INTERFACES*/, com.krake.bus.model.BusStopTime,
    RecordWithStringIdentifier, RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    override var realtimeDeparture: Long? = null
    open var stopIndex: String? = null
    override var tripId: String? = null
    override var scheduledArrival: Long? = null
    override var departureDelay: Long? = null
    override var timePoint: Boolean? = null
    override var realtime: String? = null
    open var stopId: Long? = null
    override var realtimeArrival: Long? = null
    override var scheduledDeparture: Long? = null
    open var stopCount: String? = null
    @PrimaryKey
    override var stringIdentifier: String = ""
    override var arrivalDelay: Long? = null
    open var headsign: String? = null
    open var realtimeState: String? = null
    open var serviceDay: String? = null
/*ENDFIELDS*/

    override fun isLastStop(): Boolean {
        val count = stopCount?.toIntOrNull()
        val index = stopIndex?.toIntOrNull()


        if (count != null && index != null) {
            return index == count - 1
        }

        return false
    }
}
