/**
* Created by Krake Generator 8.3 Daiquiri (1711.02.15) on 14/04/2020, 12:25**/
package mykrake.com.krakesample.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.*
import java.util.Date

open class RequestCache: RealmObject(), com.krake.core.model.RequestCache
{
    @PrimaryKey override var cacheName: String = ""
    override var dateExecuted: Date = Date()
    override var _extras: String = ""
    override var _sort: String = ""

    open var questionRecord : RealmList<QuestionRecord> = RealmList()
    open var itinerario : RealmList<Itinerario> = RealmList()
    open var userReport : RealmList<UserReport> = RealmList()
    open var userProviderEntry : RealmList<UserProviderEntry> = RealmList()
    open var immagine : RealmList<Immagine> = RealmList()
    open var user : RealmList<User> = RealmList()
    open var parcheggio : RealmList<Parcheggio> = RealmList()
    open var termPart : RealmList<TermPart> = RealmList()
    open var prodotto : RealmList<Prodotto> = RealmList()
    open var calendarEvent : RealmList<CalendarEvent> = RealmList()
    open var otpStopItem : RealmList<OtpStopItem> = RealmList()
    open var policyText : RealmList<PolicyText> = RealmList()
    open var answerRecord : RealmList<AnswerRecord> = RealmList()
    open var userPolicyAnswersRecord : RealmList<UserPolicyAnswersRecord> = RealmList()
    open var game : RealmList<Game> = RealmList()
    open var configurazioneTwitter : RealmList<ConfigurazioneTwitter> = RealmList()
    open var stopTime : RealmList<StopTime> = RealmList()
    open var pOI : RealmList<POI> = RealmList()
    open var taxonomy : RealmList<Taxonomy> = RealmList()
    open var blog : RealmList<Blog> = RealmList()
    open var pattern : RealmList<Pattern> = RealmList()
    open var questionnaire : RealmList<Questionnaire> = RealmList()
    open var blogPost : RealmList<BlogPost> = RealmList()

}
