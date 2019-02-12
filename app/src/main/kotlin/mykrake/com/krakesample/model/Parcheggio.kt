/**
 * Created by Krake Generator 10.0 Bloody Mary (1902.11.10) on 12/02/2019, 10:17**/
package mykrake.com.krakesample.model

import com.krake.core.model.*
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class Parcheggio : RealmObject() /*INTERFACES*/, ContentItemWithDescription, ContentItemWithLocation,
    ContentItemWithGallery, RecordWithAutoroute, RecordWithIdentifier, RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var autoroutePartUseCulturePattern: Boolean? = null
    override var bodyPartText: String? = null
    override var titlePartTitle: String? = null
    open var galleryFirstMediaUrl: String? = null
    open var autoroutePartPromoteToHomePage: Boolean? = null
    open var capacityValue: Long? = null
    open var contentType: String? = null
    override var galleryMediaParts: RealmList<MediaPart> = RealmList()
    @Index
    override var autoroutePartDisplayAlias: String = ""
    open var bodyPartFormat: String? = null
    @PrimaryKey
    override var identifier: Long = 0
    override var mapPart: MapPart? = null
/*ENDFIELDS*/
}
