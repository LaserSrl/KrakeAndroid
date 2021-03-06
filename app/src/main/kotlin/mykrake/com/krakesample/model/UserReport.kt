/**
 * Created by Krake Generator 8.3 Daiquiri (1711.02.15) on 14/04/2020, 12:25**/
package mykrake.com.krakesample.model

import com.krake.core.model.*
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class UserReport : RealmObject() /*INTERFACES*/,com.krake.usercontent.model.UserCreatedContent,ContentItemWithDescription,ContentItemWithLocation,RecordWithAutoroute,RecordWithIdentifier,RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var autoroutePartUseCulturePattern : Boolean? = null
    override var titlePartTitle : String? = null
    override var bodyPartText : String? = null
    open var galleryFirstMediaUrl : String? = null
    open var autoroutePartPromoteToHomePage : Boolean? = null
    open var contentType : String? = null
    override var galleryMediaParts : RealmList<MediaPart> = RealmList()
    override var publishExtensionPartPublishExtensionStatus : EnumerationField? = null
    @Index override var autoroutePartDisplayAlias : String = ""
    open var bodyPartFormat : String? = null
    @PrimaryKey override var identifier : Long = 0
    override var sottotitoloValue : String? = null
    open var secondaGalleryMediaParts : RealmList<MediaPart> = RealmList()
    override var mapPart : MapPart? = null
    open var categoriaTerms : RealmList<TermPart> = RealmList()
    open var secondaGalleryFirstMediaUrl : String? = null
/*ENDFIELDS*/
}
