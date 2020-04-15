/**
 * Created by Krake Generator 8.3 Daiquiri (1711.02.15) on 14/04/2020, 12:25**/
package mykrake.com.krakesample.model

import com.krake.core.model.*
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class Immagine : RealmObject() /*INTERFACES*/,ContentItemWithDescription,ContentItemWithGallery,RecordWithAutoroute,RecordWithIdentifier,RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var autoroutePartUseCulturePattern : Boolean? = null
    override var bodyPartText : String? = null
    open var galleryFirstMediaUrl : String? = null
    override var titlePartTitle : String? = null
    open var autoroutePartPromoteToHomePage : Boolean? = null
    @Index override var autoroutePartDisplayAlias : String = ""
    open var contentType : String? = null
    override var galleryMediaParts : RealmList<MediaPart> = RealmList()
    open var bodyPartFormat : String? = null
    open var tipoImmagineTerms : RealmList<TermPart> = RealmList()
    @PrimaryKey override var identifier : Long = 0
/*ENDFIELDS*/
}
