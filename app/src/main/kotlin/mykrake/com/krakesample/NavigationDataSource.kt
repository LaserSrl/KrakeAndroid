package mykrake.com.krakesample

import android.content.Context
import android.content.Intent
import android.util.SparseArray
import android.view.MenuItem
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.krake.bus.app.BusRouteListActivity
import com.krake.bus.app.BusSearchActivity
import com.krake.bus.component.module.BusComponentModule
import com.krake.cards.CardsActivity
import com.krake.contentcreation.ContentCreationActivity
import com.krake.contentcreation.ContentCreationTabInfo
import com.krake.contentcreation.ContentDefinition
import com.krake.contentcreation.FieldExtras
import com.krake.contentcreation.component.module.ContentCreationComponentModule
import com.krake.core.app.ContentItemListMapActivity
import com.krake.core.component.base.ComponentManager
import com.krake.core.component.module.*
import com.krake.core.drawer.NavigationItemIntentSelectionListener
import com.krake.core.login.LoginManager
import com.krake.core.login.PrivacyModificationActivity
import com.krake.core.media.MediaType
import com.krake.gamequiz.component.module.GameQuizComponentModule
import com.krake.puzzlegame.PuzzleGameSelectionActivity
import com.krake.surveys.app.SurveyActivity
import com.krake.surveys.component.module.SurveyComponentModule
import com.krake.trip.TripPlannerSearchActivity
import com.krake.usercontent.UserCreatedContentActivity
import com.krake.usercontent.component.module.UserContentComponentModule
import com.krake.youtube.YoutubeVideoActivity
import mykrake.com.krakesample.model.*
import java.util.*

/**
 * Created by antoniolig on 07/03/2017.
 */
class NavigationDataSource(val context: Context) : NavigationItemIntentSelectionListener {
    override fun createIntentForNavigationItemSelected(item: MenuItem): Intent? {
        var intent: Intent? = null

        val creationInfos = ArrayList<ContentCreationTabInfo>(3)

        // creazione del tab dei media
        creationInfos.add(
            ContentCreationTabInfo.createMediaInfo(
                com.krake.contentcreation.R.string.medias,
                "Gallery",
                "medias",
                true,
                MediaType.IMAGE or MediaType.VIDEO or MediaType.AUDIO,
                1
            )
        )

        // creazione del tab delle info
        val fields = ArrayList<ContentCreationTabInfo.FieldInfo>(4)
        fields.add(
            ContentCreationTabInfo.FieldInfo(
                com.krake.contentcreation.R.string.title,
                "TitlePart.Title",
                "titlePartTitle",
                ContentCreationTabInfo.FIELD_TYPE_TEXT,
                true
            )
        )
        fields.add(
            ContentCreationTabInfo.FieldInfo(
                com.krake.contentcreation.R.string.subtitle,
                "Sottotitolo",
                "sottotitoloValue",
                ContentCreationTabInfo.FIELD_TYPE_TEXT,
                true
            )
        )
        fields.add(
            ContentCreationTabInfo.FieldInfo(
                com.krake.contentcreation.R.string.subtitle,
                "",
                null,
                ContentCreationTabInfo.FIELD_TYPE_BOOLEAN,
                true
            )
        )
        val bodyArray = SparseArray<Int>(1)
        bodyArray.put(FieldExtras.Text.KEY_MAX_LINES, FieldExtras.Text.MAX_LINES_NO_LIMIT)
        fields.add(
            ContentCreationTabInfo.FieldInfo(
                com.krake.contentcreation.R.string.description,
                "BodyPart.Text",
                "bodyPartText",
                ContentCreationTabInfo.FIELD_TYPE_TEXT,
                true,
                bodyArray,
                null
            )
        )
        fields.add(
            ContentCreationTabInfo.FieldInfo(
                com.krake.contentcreation.R.string.category,
                "UserReport.Categoria",
                "categoriaTerms",
                ContentCreationTabInfo.FIELD_TYPE_ENUM_OR_TERM_SELECTION,
                true
            )
        )
        //fields.add(ContentCreationTabInfo.FieldInfo(R.string.public_transport, "UserReport.Punti", null, true, true, null, null, "elenco-poi"))
        creationInfos.add(ContentCreationTabInfo.createFieldsInfo(com.krake.contentcreation.R.string.infos, fields))

        // creazione del tab della mappa
        creationInfos.add(
            ContentCreationTabInfo.createMapInfo(
                com.krake.contentcreation.R.string.map,
                "MapPart",
                "mapPart",
                true
            )
        )

        // parametri impliciti
        val jsonParameters = JsonObject()
        jsonParameters.addProperty("Language", context.getString(com.krake.contentcreation.R.string.orchard_language))
        val status = JsonArray()
        status.add(JsonPrimitive(context.getString(com.krake.contentcreation.R.string.orchard_content_creation_status)))
        jsonParameters.add("PublishExtensionStatus", status)

        val userReportDefinition = ContentDefinition(
            context.getString(com.krake.contentcreation.R.string.orchard_citizen_user_reports_content_type),
            true,
            creationInfos,
            jsonParameters
        )

        when (item.itemId) {
            R.id.nav_poi -> intent = ComponentManager.createIntent()
                .from(context)
                .to(ContentItemListMapActivity::class.java)
                .with(
                    ThemableComponentModule()
                        .title("POI"),
                    OrchardComponentModule()
                        .avoidPagination()
                        .dataClass(POI::class.java)
                        .displayPath("elenco-poi")
                        .searchColumnsName("titlePartTitle", "bodyPartText"),
                    ListMapComponentModule(context)
                        .activityLayout(R.layout.activity_content_items_map_or_grid)
                        .termsModules(
                            TermsModule()
                                .filterQueryString(false),
                            OrchardComponentModule()
                                .dataClass(Taxonomy::class.java)
                                .displayPath("categorie-poi")
                        )
                        .loadDetailsByPath(true)
                        .listCellLayout(R.layout.cell_content_item_cardview)
                        .mapUseCluster(true)
                        .detailModules(DetailComponentModule(context),

                            ThemableComponentModule().apply {
                                if (context.resources.getBoolean(R.bool.is_tablet)) theme(
                                    R.style.ContentItemsDetailThemeFloating
                                )
                            })
                )
                .build()

            R.id.nav_itineraries -> intent = ComponentManager.createIntent()
                .from(context)
                .to(ContentItemListMapActivity::class.java)
                .with(
                    ThemableComponentModule()
                        .title("Itinerari"),
                    OrchardComponentModule()
                        .dataClass(Itinerario::class.java)
                        .displayPath("itinerari")
                        .deepLevel(13),
                    ListMapComponentModule(context)
                        .loadDetailsByPath(true)
                )
                .build()

            R.id.nav_myself -> {
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

                fields.add(
                    ContentCreationTabInfo.FieldInfo(
                        R.string.intolleranze, "User.Intolleranze",
                        "intolleranzeTerms", ContentCreationTabInfo.FIELD_TYPE_ENUM_OR_TERM_SELECTION, true
                    )
                )

                creationInfos.add(ContentCreationTabInfo.createFieldsInfo(R.string.you, fields))

                intent = ComponentManager.createIntent()
                    .from(context)
                    .to(ContentCreationActivity::class.java)
                    .with(
                        ContentCreationComponentModule(context)
                            .contentDefinition(
                                ContentDefinition(
                                    context.getString(R.string.orchard_user_content_type),
                                    true,
                                    creationInfos,
                                    null
                                )
                            )
                            .avoidActivityClosingAfterContentSent(true)
                            .originalObjectConnection(
                                OrchardComponentModule()
                                    .dataClass(User::class.java)
                                    .displayPath(context.getString(R.string.orchard_user_info_display_path))
                            ),
                        LoginComponentModule().loginRequired(true)
                    )
                    .build()
            }

            R.id.nav_products -> intent = ComponentManager.createIntent()
                .from(context)
                .to(ContentItemListMapActivity::class.java)
                .with(
                    ThemableComponentModule()
                        .title("Prodotti"),
                    OrchardComponentModule()
                        .dataClass(Prodotto::class.java)
                        .avoidPagination()
                        .searchColumnsName("titlePartTitle")
                        .displayPath(context.getString(R.string.orchard_path_products)),
                    ListMapComponentModule(context)
                        .showMap(false)
                        .activityLayout(R.layout.activity_content_items_map_or_grid_fab)
                )
                .build()

            R.id.nav_routes -> intent = ComponentManager.createIntent()
                .from(context)
                .to(BusRouteListActivity::class.java)
                .with(
                    BusRouteListActivity.defaultListMapModule(context),
                    BusComponentModule()
                        .patternClass(Pattern::class.java)
                        .stopItemClass(OtpStopItem::class.java)
                )
                .build()

            R.id.nav_cards -> intent = ComponentManager.createIntent()
                .from(context)
                .to(CardsActivity::class.java)
                .with(
                    ThemableComponentModule()
                        .title("Cartoline"),
                    OrchardComponentModule()
                        .dataClass(Immagine::class.java)
                        .displayPath("cartoline"),
                    ListMapComponentModule(context)
                        .loadDetailsByPath(true)
                )
                .build()

            R.id.nav_user_reports -> {
                val userModule = UserContentComponentModule(context)
                    .contentCreationModules(
                        LoginComponentModule()
                            .loginRequired(true),
                        ContentCreationComponentModule(context)
                            .contentDefinition(userReportDefinition)
                    )

                intent = ComponentManager.createIntent()
                .from(context)
                .to(UserCreatedContentActivity::class.java)
                .with(
                    ThemableComponentModule()
                        .title("UserReport"),
                    ListMapComponentModule(context),
                    LoginComponentModule()
                        .loginRequired(false),
                    OrchardComponentModule()
                        .dataClass(UserReport::class.java)
                        .displayPath(userModule.tabs.firstOrNull()?.displayAlias),
                    userModule

                )
                .build()
            }
            R.id.nav_trip -> intent = ComponentManager.createIntent()
                .from(context)
                .to(TripPlannerSearchActivity::class.java)
                .build()

            R.id.nav_bus -> intent = ComponentManager.createIntent()
                .from(context)
                .to(BusSearchActivity::class.java)
                .with(
                    BusSearchActivity.defaultListMapModule(context), BusComponentModule()
                        .patternClass(Pattern::class.java)
                        .stopItemClass(OtpStopItem::class.java)
                        .busMovementProvider(BusLocationTracker::class.java)
                )
                .build()

            R.id.nav_news -> intent = ComponentManager.createIntent()
                .from(context)
                .to(ContentItemListMapActivity::class.java)
                .with(
                    ThemableComponentModule()
                        .title("Notizie"),
                    OrchardComponentModule()
                        .dataClass(BlogPost::class.java)
                        .displayPath("elenco-notizie-blog"),
                    ListMapComponentModule(context)
                        .showMap(false)
                )
                .build()

            R.id.nav_domanda -> intent = ComponentManager.createIntent()
                .from(context)
                .to(SurveyActivity::class.java)
                .with(
                    ThemableComponentModule()
                        .title("Title"),
                    //For test try comment this:
                    LoginComponentModule().loginRequired(true),

                    SurveyComponentModule(context),
                    OrchardComponentModule()
                        .dataClass(Questionnaire::class.java)
                        .displayPath("elenco-questionari")
                )
                .build()

            R.id.nav_game -> intent = ComponentManager.createIntent()
                .from(context)
                .to(PuzzleGameSelectionActivity::class.java)
                .with(
                    GameQuizComponentModule(),
                    OrchardComponentModule()
                        .dataClass(Game::class.java)
                        .displayPath("gioco")
                )
                .build()

            R.id.nav_video -> intent = ComponentManager.createIntent()
                .from(context)
                .to(YoutubeVideoActivity::class.java)
                .with(
                    OrchardComponentModule()
                        .dataClass(Prodotto::class.java)
                        .avoidPagination()
                        .searchColumnsName("titlePartTitle")
                        .displayPath(context.getString(R.string.orchard_path_products)),
                    YoutubeVideoActivity.defaultListMapComponentModule(context)
                    /*ListMapComponentModule(context)
                        .listFragmentClass(YoutubeVideosFragment::class.java)*/
                )
                .build()

            R.id.nav_logout -> LoginManager.shared.logout()

            R.id.nav_privacy -> intent = ComponentManager.createIntent()
                .from(context)
                .to(PrivacyModificationActivity::class.java)
                .with(LoginComponentModule().loginRequired(true))
                .build()
        }

        return intent
    }
}