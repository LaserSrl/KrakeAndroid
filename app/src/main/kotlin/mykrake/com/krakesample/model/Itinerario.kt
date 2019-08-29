/**
 * Created by Krake Generator 10.0 Bloody Mary (1906.25.14) on 29/08/2019, 10:23**/
package mykrake.com.krakesample.model

import com.krake.core.model.*
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class Itinerario : RealmObject() /*INTERFACES*/, com.krake.itineraries.model.Itinerary,
    ContentItemWithDescription, ContentItemWithLocation, ContentItemWithGallery,
    RecordWithAutoroute, RecordWithIdentifier, RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var autoroutePartUseCulturePattern: Boolean? = null
    override var bodyPartText: String? = null
    override var titlePartTitle: String? = null
    open var galleryFirstMediaUrl: String? = null
    open var autoroutePartPromoteToHomePage: Boolean? = null
    open var contentType: String? = null
    override var galleryMediaParts: RealmList<MediaPart> = RealmList()
    open var bodyPartFormat: String? = null
    @Index
    override var autoroutePartDisplayAlias: String = ""
    override var puntiDiInteresseContentItems: RealmList<POI> = RealmList()
    @PrimaryKey
    override var identifier: Long = 0
    override var mapPart: MapPart? = null
/*ENDFIELDS*/
}
