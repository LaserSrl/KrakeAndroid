/**
 * Created by Krake Generator 10.0 Bloody Mary (1906.25.14) on 29/08/2019, 10:23**/
package mykrake.com.krakesample.model

import com.krake.core.model.*
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import java.util.*


open class BlogPost : RealmObject() /*INTERFACES*/, ContentItemWithDescription,
    ContentItemWithGallery, RecordWithAutoroute, RecordWithIdentifier,
    RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var blogPostPartText: String? = null
    open var tagsPartCurrentTags: String? = null
    override var galleryMediaParts: RealmList<MediaPart> = RealmList()
    open var blogPostPartTitle: String? = null
    open var publishLaterPartScheduledPublishUtc: String? = null
    open var blogPostPartBlogPartIdentifier: Long? = null
    open var blogPostPartBlogPartPostCount: Long? = null
    open var blogPostPartBlogPartName: String? = null
    open var autoroutePartPromoteToHomePage: Boolean? = null
    open var blogPostPartIsPublished: Boolean? = null
    open var bodyPartFormat: String? = null
    @Index
    override var autoroutePartDisplayAlias: String = ""
    @PrimaryKey
    override var identifier: Long = 0
    open var blogPostPartHasPublished: Boolean? = null
    open var blogPostPartPublishedUtc: Date? = null
    open var contentType: String? = null
    open var autoroutePartUseCulturePattern: Boolean? = null
    open var blogPostPartBlogPartPostsPerPage: Long? = null
    open var blogPostPartHasDraft: Boolean? = null
    open var blogPostPartBlogPartFeedProxyUrl: String? = null
    open var blogPostPartBlogPartDescription: String? = null
    override var bodyPartText: String? = null
    open var galleryFirstMediaUrl: String? = null
    override var titlePartTitle: String? = null
    open var blogPostPartBlogPartEnableCommentsFeed: Boolean? = null
/*ENDFIELDS*/
}
