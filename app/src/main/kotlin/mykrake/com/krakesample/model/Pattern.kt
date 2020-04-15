/**
 * Created by Krake Generator 8.3 Daiquiri (1711.02.15) on 14/04/2020, 12:25**/
package mykrake.com.krakesample.model

import com.krake.bus.model.BusRoute
import com.krake.core.model.RecordWithFilter
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey


open class Pattern : RealmObject() /*INTERFACES*/,com.krake.bus.model.BusPattern,RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    override var stopTimesList : RealmList<StopTime> = RealmList()
    open var patternId : String? = null
    @PrimaryKey override var stringIdentifier : String = ""
    override var descriptionText : String? = null
/*ENDFIELDS*/
@Ignore
override var busRoute: BusRoute? = null
}
