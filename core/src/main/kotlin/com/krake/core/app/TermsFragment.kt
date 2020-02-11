package com.krake.core.app

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.tabs.TabLayout
import com.krake.core.Constants
import com.krake.core.OrchardError
import com.krake.core.R
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.component.module.TermsModule
import com.krake.core.data.DataModel
import com.krake.core.media.loader.TermIconLoader
import com.krake.core.model.Taxonomy
import com.krake.core.model.TermPart
import com.krake.core.view.TabLayoutHelper
import java.util.*

open class TermsFragment : OrchardDataModelFragment(),
    TabLayout.OnTabSelectedListener,
    SwipeRefreshLayout.OnRefreshListener
{
    @BundleResolvable
    lateinit var termsModule: TermsModule
    protected var selectedTermPartIdentifier: Long = 0
    private var mListener: Listener? = null
    private val STATE_SELECTED_TAB = "selectedTab"
    private lateinit var mTabLayoutHelper: TabLayoutHelper
    private val currentTerms = LinkedList<TermPart>()

    /**
     * @return the attached [TabLayout].
     */
    protected val tabLayout: TabLayout
        get() = mTabLayoutHelper.layout()

    override fun onAttach(activity: Context)
    {
        super.onAttach(activity)
        if (activity is Listener)
        {
            mListener = activity
        }
        else
        {
            throw RuntimeException("The context " + activity.javaClass.name + " must implement " + Listener::class.java.name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        orchardComponentModule.putExtraParameter(Constants.REQUEST_RESULT_TARGET, Constants.REQUEST_SUB_TERMS)

        if (savedInstanceState != null)
            selectedTermPartIdentifier = savedInstanceState.getLong(STATE_SELECTED_TAB, 0)
    }

    override fun onConfigureModel()
    {
        internalOrchardComponentModule.startConnectionAfterActivityCreated(false)
        super.onConfigureModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        mTabLayoutHelper = createTabLayoutHelper(inflater)

        setVisibility(View.GONE)
        return tabLayout
    }

    protected open fun createTabLayoutHelper(inflater: LayoutInflater): TabLayoutHelper
    {
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return TabLayoutHelper.InflaterBuilder(inflater.context)
                .layout(R.layout.partial_tabs_scrollable)
                .layoutParams(params)
                .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        tabLayout.addOnTabSelectedListener(this)
        dataConnectionModel.restartDataLoading()
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)
        outState.putLong(STATE_SELECTED_TAB, selectedTermPartIdentifier)
    }

    override fun onDetach()
    {
        super.onDetach()
        mListener = null
    }

    override fun onDataModelChanged(dataModel: DataModel?)
    {
        val lazyList = dataModel?.listData
        if (lazyList != null && lazyList.isNotEmpty())
        {
            val taxTerms = if (lazyList[0] is Taxonomy) {
                (lazyList[0] as Taxonomy).terms
            } else {
                lazyList as List<TermPart>
            }

            onTermsLoaded(taxTerms)
        }
    }

    override fun onDataLoadingError(orchardError: OrchardError)
    {
        if (currentTerms.isEmpty())
            setVisibility(View.GONE)
    }

    private fun addTermsToTab(terms: List<TermPart>, previouslySelectedIdentifier: Long)
    {
        val showTabAll = termsModule.showTabAll
        if (showTabAll)
            mTabLayoutHelper.addTab(getString(R.string.tab_all_title),
                                    ContextCompat.getDrawable(activity!!, R.drawable.tab_all_icon),
                                    null,
                                    previouslySelectedIdentifier == 0L)

        var index = 0
        for (termPart in terms)
        {
            val tab = mTabLayoutHelper.addTab(termPart.name,
                                              null,
                                              termPart,
                                              termPart.identifier == previouslySelectedIdentifier || !showTabAll && index == 0 && previouslySelectedIdentifier == 0L)

            if (mTabLayoutHelper.showTabImage())
            {
                val spaceDrawable = GradientDrawable()
                spaceDrawable.setColor(Color.TRANSPARENT)
                mTabLayoutHelper.setTabIcon(tab, spaceDrawable)

                TermIconLoader.loadTerms(this, termPart, object : com.krake.core.media.loader.TermIconLoader.OnTermIconLoadListener
                {
                    /**
                     * Notifica che il caricamento è stato completato con successo.
                     *
                     * @param icon icona scaricata da WS o caricata dalle risorse
                     */
                    override fun onIconLoadCompleted(icon: Drawable, fromWs: Boolean)
                    {
                        mTabLayoutHelper.setTabIcon(tab, icon)
                    }

                    /**
                     * Notifica che il caricamento è fallito.
                     * Se la [TermPart] non ha un'icona, questo metodo viene richiamato automaticamente.
                     */
                    override fun onIconLoadFailed(fromWs: Boolean)
                    { /* empty */
                    }
                })
            }
            ++index
        }
    }

    private fun needToUpdateTabs(terms: List<TermPart>): Boolean
    {
        var index = if (termsModule.showTabAll) 1 else 0

        val tabLayout = tabLayout

        val count = tabLayout.tabCount
        if (count != 0 && terms.size == tabLayout.tabCount - index)
        {
            for (termPart in terms)
            {
                val tabTag = tabLayout.getTabAt(index)?.tag as? TermPart
                if (tabTag?.identifier != termPart.identifier)
                    return true
                ++index
            }

            return false
        }
        else
            return true
    }

    private fun setVisibility(visibility: Int)
    {
        tabLayout.visibility = visibility
    }

    /**
     * Called when a tab enters the selected state.
     *
     * @param tab The tab that was selected
     */
    override fun onTabSelected(tab: TabLayout.Tab)
    {
        val termPart = getTermFromTab(tab)
        selectedTermPartIdentifier = termPart?.identifier ?: 0
        mListener?.selectedFilterTermPart(termPart, termsModule)
    }

    /**
     * Called when a tab exits the selected state.
     *
     * @param tab The tab that was unselected
     */
    override fun onTabUnselected(tab: TabLayout.Tab)
    {
        val termPart = getTermFromTab(tab)
        selectedTermPartIdentifier = termPart?.identifier ?: 0
        mListener?.unselectedFilterTermPart(termPart, termsModule)
    }

    /**
     * Called when a tab that is already selected is chosen again by the user. Some applications
     * may use this action to return to the top level of a category.
     *
     * @param tab The tab that was reselected.
     */
    override fun onTabReselected(tab: TabLayout.Tab)
    { /* empty */
    }

    /**
     * Called when a list of [TermPart] is loaded (also if it's empty).
     *
     * @param terms list of loaded [TermPart].
     */
    @CallSuper
    protected open fun onTermsLoaded(terms: List<TermPart>)
    {
        if (terms.size > 1)
        {
            setVisibility(View.VISIBLE)
            currentTerms.clear()
            val currentTermToLoad = StringTokenizer(terms[0].fullPath, "/").countTokens()

            for (termPart in terms)
            {
                val tokenizer = StringTokenizer(termPart.fullPath, "/")
                if (tokenizer.countTokens() == currentTermToLoad)
                    currentTerms.add(termPart)
            }

            if (needToUpdateTabs(currentTerms))
            {
                tabLayout.removeAllTabs()
                val previouslySelectedIdentifier = selectedTermPartIdentifier
                addTermsToTab(currentTerms, previouslySelectedIdentifier)
            }
        }
    }

    /**
     * Get the instance of a [TermPart] related to a tab.
     *
     * @param tab [TabLayout.Tab] used to identify the [TermPart].
     * @return [TermPart] related to the tab.
     */
    protected fun getTermFromTab(tab: TabLayout.Tab): TermPart?
    {
        return tab.tag as TermPart?
    }

    override fun onRefresh() {
        dataConnectionModel.page = 1
        dataConnectionModel.loadDataFromRemote()
    }

    /**
     * Listener used to notify changes on the selected [TermPart].
     */
    interface Listener
    {
        /**
         * Called when a new [TermPart] is selected.
         *
         * @param termPart    selected [TermPart].
         * @param termsModule [TermsModule] used in [TermsFragment].
         */
        fun selectedFilterTermPart(termPart: TermPart?, termsModule: TermsModule)

        /**
         * Called when a new [TermPart] is unselected.
         *
         * @param termPart    unselected [TermPart].
         * @param termsModule [TermsModule] used in [TermsFragment].
         */
        fun unselectedFilterTermPart(termPart: TermPart?, termsModule: TermsModule)
    }

    companion object
    {
        fun newInstance(args: Bundle): TermsFragment
        {
            val fragment = TermsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}