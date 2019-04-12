package mykrake.com.krakesample

import com.krake.cards.CardsDetailFragment
import com.krake.contentcreation.ContentCreationActivity
import com.krake.contentcreation.ContentCreationTabInfo
import com.krake.contentcreation.ContentDefinition
import com.krake.contentcreation.component.module.ContentCreationComponentModule
import com.krake.core.app.ContentItemDetailModelFragment
import com.krake.core.app.KrakeApplication
import com.krake.core.component.base.ComponentManager
import com.krake.core.component.module.DetailComponentModule
import com.krake.core.component.module.LoginComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.extension.putModules
import com.krake.core.login.LoginManager
import com.krake.core.widget.UserNavigationMenuView
import com.krake.core.widget.UserNavigationViewListener
import com.krake.facebook.FacebookDetailSharingIntercept
import com.krake.facebook.FacebookLogoutListener
import com.krake.itineraries.ItineraryComponentModule
import com.krake.usercontent.UserContentDetailFragment
import com.squareup.leakcanary.LeakCanary
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import mykrake.com.krakesample.model.Immagine
import mykrake.com.krakesample.model.Itinerario
import mykrake.com.krakesample.model.User
import mykrake.com.krakesample.model.UserReport
import java.util.*

/**
 * Created by antoniolig on 07/03/2017.
 */
class KrakeApp : KrakeApplication(), UserNavigationViewListener {

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)

        LoginManager.shared.isLogged.observeForever(FacebookLogoutListener())

        /*beaconManger = EstimoteBeaconManager(this, BeaconRegion("Tutti", null, null, null))
        val near = ProximityBeaconRanger(this, beaconManger)
        beaconManger!!.addObserver(near)
        beaconManger!!.startRegionMonitoring()*/
        registerDetailFragment(Itinerario::class.java, ContentItemDetailModelFragment::class.java) { original ->
            val module = DetailComponentModule(this@KrakeApp)
            module.readContent(this@KrakeApp, original)
            module.contentLayout(ItineraryComponentModule.DEFAULT_DETAIL_CONTENT_LAYOUT)
            module.rootLayout(ItineraryComponentModule.DEFAULT_DETAIL_ROOT_LAYOUT)
            original.putModules(this@KrakeApp, module)
            original
        }
        //registerDetailFragment(OtpStopItem::class.java, BusStopDetailFragment::class.java)
        addDetailSharingIntent(FacebookDetailSharingIntercept())
        registerDetailFragment(UserReport::class.java, UserContentDetailFragment::class.java)
        registerDetailFragment(Immagine::class.java, CardsDetailFragment::class.java)

        val authConfig = TwitterAuthConfig(
            getString(R.string.twitter_consume_key),
            getString(R.string.twitter_consume_secret)
        )

        val config = TwitterConfig.Builder(this)
            .twitterAuthConfig(authConfig)
            .build()
        Twitter.initialize(config)
    }

    override fun userDidClick(onView: UserNavigationMenuView) {


        val creationInfos = ArrayList<ContentCreationTabInfo>()

        val fields: MutableList<ContentCreationTabInfo.FieldInfo> = ArrayList()
        fields.add(
            ContentCreationTabInfo.FieldInfo(
                R.string.sex,
                "ProfilePart.Sesso",
                "sesso.value",
                ContentCreationTabInfo.FIELD_TYPE_ENUM_OR_TERM_SELECTION,
                true
            )
        )
        fields.add(
            ContentCreationTabInfo.FieldInfo(
                R.string.nation,
                "ProfilePart.Nazionalita",
                "nazionalita.value",
                ContentCreationTabInfo.FIELD_TYPE_ENUM_OR_TERM_SELECTION,
                true
            )
        )

        creationInfos.add(ContentCreationTabInfo.createFieldsInfo(R.string.you, fields))

        val intent = ComponentManager.createIntent()
            .from(this)
            .to(ContentCreationActivity::class.java)
            .with(
                ContentCreationComponentModule(this)
                    .contentDefinition(
                        ContentDefinition(
                            getString(R.string.orchard_user_content_type),
                            true,
                            creationInfos,
                            null
                        )
                    )
                    .avoidActivityClosingAfterContentSent(true)
                    .originalObjectConnection(
                        OrchardComponentModule()
                            .dataClass(User::class.java)
                            .displayPath(getString(R.string.orchard_user_info_display_path))
                    ),
                LoginComponentModule().loginRequired(true)
            )
            .build()

        startActivity(intent)
    }
}