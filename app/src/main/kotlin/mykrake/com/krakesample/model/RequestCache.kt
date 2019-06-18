/**
 * Created by Krake Generator 10.0 Bloody Mary (1905.28.11) on 18/06/2019, 11:43**/
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
    open var itinerario: RealmList<Itinerario> = RealmList()
    open var userReport: RealmList<UserReport> = RealmList()
    open var immagine: RealmList<Immagine> = RealmList()
    open var user: RealmList<User> = RealmList()
    open var parcheggio: RealmList<Parcheggio> = RealmList()
    open var termPart: RealmList<TermPart> = RealmList()
    open var prodotto: RealmList<Prodotto> = RealmList()
    open var calendarEvent: RealmList<CalendarEvent> = RealmList()
    open var otpStopItem: RealmList<OtpStopItem> = RealmList()
    open var policyText: RealmList<PolicyText> = RealmList()
    open var answerRecord: RealmList<AnswerRecord> = RealmList()
    open var userPolicyAnswersRecord: RealmList<UserPolicyAnswersRecord> = RealmList()
    open var game: RealmList<Game> = RealmList()
    open var stopTime: RealmList<StopTime> = RealmList()
    open var configurazioneTwitter: RealmList<ConfigurazioneTwitter> = RealmList()
    open var pOI: RealmList<POI> = RealmList()
    open var taxonomy: RealmList<Taxonomy> = RealmList()
    open var pattern: RealmList<Pattern> = RealmList()
    open var blog: RealmList<Blog> = RealmList()
    open var questionnaire: RealmList<Questionnaire> = RealmList()
    open var blogPost: RealmList<BlogPost> = RealmList()
/*ENDFIELDS*/
}
