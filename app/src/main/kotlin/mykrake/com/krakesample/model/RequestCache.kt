/**
 * Created by Krake Generator 10.0 Bloody Mary (1903.22.10) on 09/05/2019, 09:16**/
package mykrake.com.krakesample.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class RequestCache : RealmObject(), com.krake.core.model.RequestCache {
    @PrimaryKey
    override var cacheName: String = ""
    override var dateExecuted: Date = Date()
    override var _extras: String = ""
    override var _sort: String = ""

    /*FIELDS*/
    open var questionRecord: RealmList<QuestionRecord> = RealmList()
    open var answerRecord: RealmList<AnswerRecord> = RealmList()
    open var game: RealmList<Game> = RealmList()
    open var user: RealmList<User> = RealmList()
    open var configurazioneTwitter: RealmList<ConfigurazioneTwitter> = RealmList()
    open var questionnaire: RealmList<Questionnaire> = RealmList()
    open var policyText: RealmList<PolicyText> = RealmList()
    open var prodotto: RealmList<Prodotto> = RealmList()
    open var pOI: RealmList<POI> = RealmList()
    open var userReport: RealmList<UserReport> = RealmList()
    open var taxonomy: RealmList<Taxonomy> = RealmList()
    open var blogPost: RealmList<BlogPost> = RealmList()
    open var termPart: RealmList<TermPart> = RealmList()
    open var calendarEvent: RealmList<CalendarEvent> = RealmList()
    open var userPolicyAnswersRecord: RealmList<UserPolicyAnswersRecord> = RealmList()
    open var itinerario: RealmList<Itinerario> = RealmList()
    open var blog: RealmList<Blog> = RealmList()
    open var parcheggio: RealmList<Parcheggio> = RealmList()
    open var immagine: RealmList<Immagine> = RealmList()
/*ENDFIELDS*/
}
