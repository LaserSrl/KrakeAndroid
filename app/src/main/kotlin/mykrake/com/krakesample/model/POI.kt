/**
 * Created by Krake Generator 8.3 Daiquiri (1711.02.15) on 14/04/2020, 12:25**/
package mykrake.com.krakesample.model

import com.krake.core.model.*
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class POI : RealmObject() /*INTERFACES*/,ContentItemWithDescription,ContentItemWithLocation,ContentItemWithGallery,RecordWithShare,RecordWithAutoroute,RecordWithIdentifier,RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var autoroutePartUseCulturePattern : Boolean? = null
    override var titlePartTitle : String? = null
    override var bodyPartText : String? = null
    open var galleryFirstMediaUrl : String? = null
    open var autoroutePartPromoteToHomePage : Boolean? = null
    override var shareLinkPart : ShareLinkPart? = null
    open var contentType : String? = null
    override var galleryMediaParts : RealmList<MediaPart> = RealmList()
    @Index override var autoroutePartDisplayAlias : String = ""
    open var bodyPartFormat : String? = null
    @PrimaryKey override var identifier : Long = 0
    open var categoriaPOITerms : RealmList<TermPart> = RealmList()
    open var tempoDiVisitaValue : Long? = null
    override var mapPart : MapPart? = null
/*ENDFIELDS*/
}
