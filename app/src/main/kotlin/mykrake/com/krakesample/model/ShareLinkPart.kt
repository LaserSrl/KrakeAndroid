/**
 * Created by Krake Generator 10.0 Bloody Mary (1903.22.10) on 09/05/2019, 09:16**/
package mykrake.com.krakesample.model

import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithIdentifier
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class ShareLinkPart : RealmObject() /*INTERFACES*/, com.krake.core.model.ShareLinkPart, RecordWithIdentifier,
    RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    override var sharedText: String? = null
    open var sharedBody: String? = null
    override var sharedLink: String? = null
    open var sharedIdImage: String? = null
    override var sharedImage: String? = null
    @PrimaryKey
    override var identifier: Long = 0
/*ENDFIELDS*/
}
