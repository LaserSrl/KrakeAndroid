/**
 * Created by Krake Generator 10.0 Bloody Mary (1906.25.14) on 29/08/2019, 10:23**/
package mykrake.com.krakesample.model

import com.krake.core.model.*
import com.krake.youtube.model.YoutubeVideo
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class Prodotto : RealmObject() /*INTERFACES*/, ContentItemWithDescription,
    ContentItemWithGallery, RecordWithShare, RecordWithAutoroute, RecordWithIdentifier,
    RecordWithFilter/*ENDINTERFACES*/, YoutubeVideo
, ContentItemWithSocial
{
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

    override val facebookValue: String?
        get() = "https://www.google.com"
    override val instagramValue: String?
        get() = "https://www.google.com"
    override val pinterestValue: String?
        get() = ""
    override val twitterValue: String?
        get() = null
    override val youtubeValue: String?
        get() = videoUrlValue


}
