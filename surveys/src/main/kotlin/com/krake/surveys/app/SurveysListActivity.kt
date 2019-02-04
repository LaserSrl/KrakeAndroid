package com.krake.surveys.app

import android.content.Intent
import android.os.Bundle
import com.krake.core.app.ContentItemDetailActivity
import com.krake.core.app.ContentItemListMapActivity
import com.krake.core.component.base.ComponentManager
import com.krake.core.component.base.ComponentModule
import com.krake.core.component.module.ListMapComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.component.module.ThemableComponentModule
import com.krake.core.extension.putModules
import com.krake.core.model.ContentItem
import com.krake.surveys.component.module.SurveyComponentModule

/**
 * Created by joel on 07/08/17.
 */

open class SurveysListActivity : ContentItemListMapActivity() {

    private val SURVEY_DETAIL_CODE = 228

    override fun onShowContentItemDetails(sender: Any, contentItem: ContentItem)
    {
        val detailIntent = getDetailIntent(contentItem)

        startActivityForResult(detailIntent, SURVEY_DETAIL_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (requestCode == SURVEY_DETAIL_CODE)
        {
            onRefresh()
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun getDetailIntent(contentItem: ContentItem): Intent {
        return ComponentManager.createIntent()
                .from(this)
                .to(SurveyActivity::class.java)
                .put(detailBundleForItem(contentItem))
                .build()
    }

    /**
     * Crea il [Bundle] che verrà passato alla [ContentItemDetailActivity] dopo aver effettuato il tap su una cella.
     * Utilizzando [ListMapComponentModule.getDetailBundle] come [Bundle] di partenza, vengono sovrascritti alcuni [ComponentModule]
     * usando delle proprietà relative al [ContentItem] scelto.
     * @param contentItem elemento della lista selezionato.
     * @return [Bundle] con le proprietà relative al [ContentItem] selezionato.
     */
    override fun detailBundleForItem(contentItem: ContentItem): Bundle {
        val detailBundle = listMapComponentModule.detailBundle
        val surveyModule = SurveyComponentModule(this)
        val themableModule = ThemableComponentModule()
        themableModule.readContent(this, detailBundle)
        themableModule.upIntent(getDetailUpNavigationIntent(contentItem))
        surveyModule.readContent(this, detailBundle)

        val orchardModule = OrchardComponentModule()
        orchardModule.readContent(this, detailBundle)
        orchardModule.dataClass(contentItem.javaClass)
        orchardModule.record(contentItem)


        /* Necessario per evitare delle references circolari che non sono supportate dai Parcelable. */
        val bundle = Bundle()
        bundle.putAll(detailBundle)
        bundle.putModules(this, themableModule, orchardModule, surveyModule)
        return bundle
    }
}