/**
 * Created by Krake Generator 10.0 Bloody Mary (1905.28.11) on 18/06/2019, 11:43**/
package mykrake.com.krakesample.model

import com.krake.core.model.ContentItem
import com.krake.core.model.RecordWithAutoroute
import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithIdentifier
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class ConfigurazioneTwitter : RealmObject() /*INTERFACES*/, com.krake.twitter.model.TweetsLoadingConfiguration,
    ContentItem, RecordWithAutoroute, RecordWithIdentifier, RecordWithFilter/*ENDINTERFACES*/ {
    /*FIELDS*/
    open var autoroutePartPromoteToHomePage: Boolean? = null
    override var titlePartTitle: String? = null
    open var contentType: String? = null
    open var autoroutePartUseCulturePattern: Boolean? = null
    override var filtroValue: String = ""
    @PrimaryKey
    override var identifier: Long = 0
    @Index
    override var autoroutePartDisplayAlias: String = ""
/*ENDFIELDS*/
}
