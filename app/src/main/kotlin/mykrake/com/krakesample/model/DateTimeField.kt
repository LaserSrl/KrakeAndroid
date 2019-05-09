/**
 * Created by Krake Generator 10.0 Bloody Mary (1903.22.10) on 09/05/2019, 09:16**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import io.realm.RealmObject
import java.util.*


open class DateTimeField : RealmObject() /*INTERFACES*/, RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var dateTime: Date? = null
    open var display: String? = null
/*ENDFIELDS*/
}
