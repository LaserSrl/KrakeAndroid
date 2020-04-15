/**
 * Created by Krake Generator 8.3 Daiquiri (1711.02.15) on 14/04/2020, 12:25**/
package mykrake.com.krakesample.model

import com.krake.core.model.MediaPart
import com.krake.core.model.RecordWithFilter
import com.krake.core.model.RecordWithIdentifier
import com.krake.core.model.User
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class User : RealmObject() /*INTERFACES*/,RecordWithIdentifier,RecordWithFilter/*ENDINTERFACES*/, User {
    override val firstPhoto: MediaPart?
        get() = null //To change initializer of created properties use File | Settings | File Templates.

    override val name: String?
        get() = "Test" //To change initializer of created properties use File | Settings | File Templates.
    override val surname: String?
        get() = "O" //To change initializer of created properties use File | Settings | File Templates.
    /*FIELDS*/
    open var contentType : String? = null
    open var userPwdRecoveryPart : UserPwdRecoveryPart? = null
    open var sesso : EnumerationField? = null
    @PrimaryKey override var identifier : Long = 0
    open var userProvidersPart : UserProvidersPart? = null
    open var favoriteCulturePart : FavoriteCulturePart? = null
    open var religioneValue : String? = null
    open var nazionalita : EnumerationField? = null
    open var intolleranzeTerms : RealmList<TermPart> = RealmList()
    open var userPolicyPartUserPolicyAnswers : RealmList<UserPolicyAnswersRecord> = RealmList()
    open var nascita : DateTimeField? = null
/*ENDFIELDS*/


}
