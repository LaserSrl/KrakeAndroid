/**
 * Created by Krake Generator 10.0 Bloody Mary (1903.22.10) on 09/05/2019, 09:16**/
package mykrake.com.krakesample.model

import com.krake.core.model.ContentItem
import com.krake.core.model.RecordWithAutoroute
import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithIdentifier
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class Blog : RealmObject() /*INTERFACES*/, ContentItem, RecordWithAutoroute, RecordWithIdentifier,
    RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var autoroutePartUseCulturePattern: Boolean? = null
    override var titlePartTitle: String? = null
    open var blogPartName: String? = null
    open var blogPartDescription: String? = null
    open var blogPartFeedProxyUrl: String? = null
    open var contentType: String? = null
    @Index
    override var autoroutePartDisplayAlias: String = ""
    open var blogPartEnableCommentsFeed: Boolean? = null
    open var blogPartPostsPerPage: Long? = null
    @PrimaryKey
    override var identifier: Long = 0
    open var autoroutePartPromoteToHomePage: Boolean? = null
    open var blogPartIdentifier: Long? = null
    open var blogPartPostCount: Long? = null
/*ENDFIELDS*/
}
