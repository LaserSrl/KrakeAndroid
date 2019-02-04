package com.krake.core.component.module

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import com.krake.core.R
import com.krake.core.app.ContentItemDetailActivity
import com.krake.core.app.ContentItemDetailModelFragment
import com.krake.core.component.base.ComponentModule

/**
 * Modulo utilizzato per specificare gli attributi di un dettaglio.
 * Utilizzato principalmente da:
 * <ul>
 * <li>[ContentItemDetailActivity] e dalle [Activity] che la estendono</li>
 * <li>[ContentItemDetailModelFragment] e dai [Fragment] che lo estendono</li>
 * </ul>
 */
class DetailComponentModule(context: Context) : ComponentModule {
    companion object {
        private const val ARG_CONTENT_LAYOUT = "argContentLayout"
        private const val ARG_DISABLE_ANALYTICS = "argDisableAnalytics"
        private const val ARG_ENABLE_SOCIAL_SHARING = "argEnableSocial"
        private const val ARG_ROOT_LAYOUT = "argRootLayout"
        private const val ARG_ANALYTICS_BUNDLE = "argAnalyticsBundle"
    }

    @LayoutRes
    var contentLayout: Int
        private set

    var disableAnalytics: Boolean
        private set

    var enableSocialSharing: Boolean
        private set

    @LayoutRes
    var rootLayout: Int
        private set

    var analyticsExtras: Bundle? = null
        private set

    init {
        contentLayout = R.layout.fragment_base_content_item
        disableAnalytics = false
        enableSocialSharing = context.resources.getBoolean(R.bool.enable_social_sharing_on_details)
        rootLayout = R.layout.fragment_detail_coordinator
    }

    /**
     * Specifica parametri aggiuntivi da passare alla chiamare di analytics.
     * Tutti i parametri specificati inviati a firebase.
     */
    fun analyticsExtras(extras: Bundle?) = apply { this.analyticsExtras = extras }

    /**
     * Specifica il layout del [ContentItemDetailModelFragment] nel quale mostrare il contenuto.
     * Se [rootLayout] avrà valore 0, il layout verrà utilizzato come root layout del fragment,
     * altrimenti verrà inserito all'interno di [rootLayout].
     * DEFAULT: [R.layout.fragment_base_content_item]
     *
     * @param contentLayout layout resource da utilizzare per mostrare il contenuto del [ContentItemDetailModelFragment]
     */
    fun contentLayout(@LayoutRes contentLayout: Int) = apply { this.contentLayout = contentLayout }

    /**
     * Specifica se disattivare analytics per il singolo [ContentItemDetailModelFragment].
     * DEFAULT: false
     *
     * @param disableAnalytics se true, analytics verrà disabilitato per il singolo [ContentItemDetailModelFragment].
     */
    fun disableAnalytics(disableAnalytics: Boolean) = apply { this.disableAnalytics = disableAnalytics }

    /**
     * Specifica se abilitare o meno la condivisione del contenuto.
     * DEFAULT: [R.bool.enable_social_sharing_on_details]
     *
     * @param enableSocialSharing se true e se il [ContentItem] può essere condiviso, verrà presentato il pulsante di condivisione.
     */
    fun enableSocialSharing(enableSocialSharing: Boolean) = apply { this.enableSocialSharing = enableSocialSharing }

    /**
     * Specifica il layout di base del [ContentItemDetailModelFragment].
     * Se utilizzato 0, il layout base sarà [contentLayout], altrimenti [contentLayout] verrà inserito al suo interno.
     * DEFAULT: [R.layout.fragment_detail_coordinator]
     *
     * @param rootLayout layout base di [ContentItemDetailModelFragment].
     */
    fun rootLayout(@LayoutRes rootLayout: Int) = apply { this.rootLayout = rootLayout }

    /**
     * Legge il contenuto di un [Bundle] e modifica le sue proprietà.
     *
     * @param context il [Context] utilizzato per leggere il [Bundle].
     * @param bundle container dal quale vengono letti i vari arguments.
     */
    override fun readContent(context: Context, bundle: Bundle) {
        contentLayout = bundle.getInt(ARG_CONTENT_LAYOUT, contentLayout)
        disableAnalytics = bundle.getBoolean(ARG_DISABLE_ANALYTICS, disableAnalytics)
        enableSocialSharing = bundle.getBoolean(ARG_ENABLE_SOCIAL_SHARING, enableSocialSharing)
        rootLayout = bundle.getInt(ARG_ROOT_LAYOUT, rootLayout)
        analyticsExtras = bundle.getBundle(ARG_ANALYTICS_BUNDLE)
    }

    /**
     * Scrive le proprietà di un modulo su un [Bundle].
     *
     * @param context il [Context] utilizzato per creare il [Bundle] e scrivere su di esso.
     * @return container che contiene le proprietà del modulo corrente.
     */
    override fun writeContent(context: Context): Bundle {
        val bundle = Bundle()
        bundle.putInt(ARG_CONTENT_LAYOUT, contentLayout)
        bundle.putBoolean(ARG_DISABLE_ANALYTICS, disableAnalytics)
        bundle.putBoolean(ARG_ENABLE_SOCIAL_SHARING, enableSocialSharing)
        bundle.putInt(ARG_ROOT_LAYOUT, rootLayout)
        analyticsExtras?.let { bundle.putBundle(ARG_ANALYTICS_BUNDLE, it) }
        return bundle
    }

    /**
     * Definisce la lista delle classi da cui questo modulo deve dipendere.
     *
     * @return array di dipendenze.
     */
    override fun moduleDependencies(): Array<Class<out ComponentModule>> {
        return arrayOf(LoginComponentModule::class.java)
    }
}