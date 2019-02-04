package com.krake.core.app

import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import com.krake.core.OrchardError
import com.krake.core.data.DataConnectionModel
import com.krake.core.data.DataListener
import com.krake.core.data.DataModel
import com.krake.core.media.MediaSelectionListener
import com.krake.core.model.ContentItem
import com.krake.core.model.ContentItemWithDescription
import com.krake.core.model.ContentItemWithGallery
import com.krake.core.model.MediaPart
import io.realm.RealmModel

/**
 * Fragment per mostrare le info di base di un oggetto orchard.
 * **Nota** e' necessario che la classe sia conforme all'interfaccia [ContentItem] o sue estensioni.
 * Il fragment mostra
 *
 *  * titolo e actionbar, utilizzando la classe [CollapsingToolbarLayout]
 *  * se l'oggetto implementa l'interfaccia [ContentItemWithGallery] viene inserita anche la gallery per mostrare i [MediaPart]
 *  * se l'oggetto implementa l'intefaccia [ContentItemWithDescription] sarà mostrata anche la descrizione dell'oggetto
 *
 *
 *
 * Gli arguments del fragment devono essere derivati dai metodi della classe [DataConnectionModel].
 * In caso non sia presente questo parametro l'indicazione viene presa dal res R.bool.enable_social_sharing_on_details
 * Se viene abilitata la condivisione sui social il fragment inserira' un elemento nell'[AppBarLayout]
 * Il menu utilizzato viene prelevato dalla reference R.menu.menu_content_item_detail.
 * Ne esiste una versione gia' pronta: R.menu.menu_content_item_detail_share_intent
 * il click sul menu item apre un [com.krake.core.widget.IntentPickerSheetView] che condividerà le informazioni inserite in [.createSharingIntent].
 *
 *\
 * L'activity che include il fragment deve implementare l'interfaccia [MediaSelectionListener]
 *
 *
 * [ContentItem]
 * [ContentItemWithDescription]
 * [ContentItemWithGallery]
 */
@Deprecated("Use ContentItemDetailModelFragment")
open class ContentItemDetailFragment : ContentItemDetailModelFragment(), DataListener
{

    override fun needToAccessDataInMultiThread(): Boolean
    {
        return super<ContentItemDetailModelFragment>.needToAccessDataInMultiThread()
    }

    override fun onDataModelChanged(dataModel: DataModel?)
    {

        if (dataModel != null)
            onDefaultDataLoaded(dataModel.listData, dataModel.cacheValid)
    }

    override fun onDataLoadingError(orchardError: OrchardError)
    {
        onDefaultDataLoadFailed(orchardError, dataConnectionModel.model.value != null)
    }

    fun getOrchardConnection(): DataConnectionModel
    {
        return dataConnectionModel
    }

    override fun onDefaultDataLoadFailed(error: OrchardError, cachePresent: Boolean)
    {

    }

    override fun onDefaultDataLoaded(list: List<RealmModel>, cacheValid: Boolean)
    {
        super.onDataModelChanged(DataModel(list, cacheValid))
    }
}

