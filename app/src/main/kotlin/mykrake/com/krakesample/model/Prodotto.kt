/**
 * Created by Krake Generator 10.0 Bloody Mary (1903.22.10) on 09/05/2019, 09:16**/
package mykrake.com.krakesample.model

import com.krake.core.model.*
import com.krake.youtube.model.YoutubeVideo
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class Prodotto : RealmObject() /*INTERFACES*/, ContentItemWithDescription, ContentItemWithGallery, RecordWithShare,
    RecordWithAutoroute, RecordWithIdentifier, RecordWithFilter/*ENDINTERFACES*/, YoutubeVideo {
    /*FIELDS*/
    open var autoroutePartUseCulturePattern: Boolean? = null
    override var bodyPartText: String? = null
    override var titlePartTitle: String? = null
    open var galleryFirstMediaUrl: String? = null
    open var autoroutePartPromoteToHomePage: Boolean? = null
    override var shareLinkPart: ShareLinkPart? = null
    @Index
    override var autoroutePartDisplayAlias: String = ""
    open var contentType: String? = null
    override var galleryMediaParts: RealmList<MediaPart> = RealmList()
    open var bodyPartFormat: String? = null
    @PrimaryKey
    override var identifier: Long = 0
    open var prezzoValue: Long? = null
    /*ENDFIELDS*/
    override val videoUrlValue: String
        get() = "https://www.youtube.com/watch?v=C0DPdy98e4c"
}
