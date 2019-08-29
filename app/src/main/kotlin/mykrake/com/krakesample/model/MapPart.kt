/**
 * Created by Krake Generator 10.0 Bloody Mary (1906.25.14) on 29/08/2019, 10:23**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithIdentifier
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class MapPart : RealmObject() /*INTERFACES*/, com.krake.core.model.MapPart,
    RecordWithIdentifier, RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    override var latitude: Double = 0.0
    override var longitude: Double = 0.0
    override var mapSourceFileMediaParts: RealmList<MediaPart> = RealmList()
    @PrimaryKey
    override var identifier: Long = 0
    open var mapSourceFileFirstMediaUrl: String? = null
    override var locationAddress: String? = null
    override var locationInfo: String? = null
/*ENDFIELDS*/
}
