/**
 * Created by Krake Generator 10.0 Bloody Mary (1906.25.14) on 29/08/2019, 10:23**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey


open class OtpStopItem : RealmObject() /*INTERFACES*/, com.krake.bus.model.BusStop,
    RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    override var dist: Long? = null
    open var code: String? = null
    override var lon: Double = 0.0
    @PrimaryKey
    override var stringIdentifier: String = ""
    override var lat: Double = 0.0
    open var originalId: String? = null
    override var name: String = ""
/*ENDFIELDS*/

    override val id: String
        get() = originalId!!

    @Ignore
    override var isMainStop: Boolean = true
}
