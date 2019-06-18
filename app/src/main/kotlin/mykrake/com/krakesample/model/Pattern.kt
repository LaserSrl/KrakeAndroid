/**
 * Created by Krake Generator 10.0 Bloody Mary (1905.28.11) on 18/06/2019, 11:43**/
package mykrake.com.krakesample.model

import com.krake.bus.model.BusRoute
import com.krake.core.model.RecordWithFilter
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey


open class Pattern : RealmObject() /*INTERFACES*/, com.krake.bus.model.BusPattern, RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    override var stopTimesList: RealmList<StopTime> = RealmList()
    open var patternId: String? = null
    @PrimaryKey
    override var stringIdentifier: String = ""
    override var descriptionText: String? = null
/*ENDFIELDS*/
@Ignore
override var busRoute: BusRoute? = null
}
