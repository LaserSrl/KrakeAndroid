package com.krake.core.app

import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ProgressBar
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.krake.core.ClassUtils
import com.krake.core.OrchardError
import com.krake.core.R
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.component.base.ComponentModule
import com.krake.core.component.module.ListMapComponentModule
import com.krake.core.content.UpdatebleOrchardDataLoader
import com.krake.core.data.DataConnectionModel
import com.krake.core.data.DataModel
import com.krake.core.model.ContentItem
import com.krake.core.model.ContentItemWithGallery
import com.krake.core.view.ListViewPagerSupport
import com.krake.core.widget.*

/**
 * Classe per mostare su una griglia o lista contenuti prelevati da Orchard.
 * I dati devono implementare l'interfaccia [ContentItem], se implementano anche [ContentItemWithGallery]
 * sarà mostrata anche la prima foto del contenuto.
 *
 *
 * Gli argument del fragment devono includere quanto specificato da [DataConnectionModel].
 * I dati vengono mostrati sulla lista utilizzando l'adapter specificato dal metodo [.createNewAdapter].
 * E possibile sovrascrivere questo metodo per personalizzare l'adapter.
 *
 *
 * Il fragment carica i dati paginati, quando l'utente scrolla verso il fondo della lista il fragment so iccupa di chiamare la pagina successiva.
 *
 *
 * **Importante** l'activity che utilizza questo fragment deve implementare l'interfaccia [OnContentItemSelectedListener]
 */
open class ContentItemGridModelFragment : OrchardDataModelFragment(),
    UpdatebleOrchardDataLoader,
    ObjectsRecyclerViewAdapter.ClickReceiver<ContentItem>,
    SwipeRefreshLayout.OnRefreshListener
{
    @BundleResolvable
    lateinit var listMapComponentModule: ListMapComponentModule

    private var mListener: OnContentItemSelectedListener? = null
    lateinit var recycleView: RecyclerView
        private set

    protected lateinit var adapter: ContentItemAdapter
        private set

    private var mNoElementsSnack: Snackbar? = null

    protected var progressBar: ProgressBar? = null
        private set

    private val mListMapInflatedModuleCoder = object : FragmentInflatedModuleCoder<ListMapComponentModule>(R.styleable.content_item_grid)
    {
        /**
         * Permette di copiare gli attributi risolti in precedenza in un [ComponentModule]
         * Non c'è bisogno di richiamare il metodo [TypedArray.recycle] perché verrà richiamato automaticamente.
         *
         * @param attrs  attributi salvati in precedenza.
         * @param module [ComponentModule] sul quale verranno copiati gli attributi.
         */
        public override fun copyToModule(attrs: TypedArray, module: ListMapComponentModule)
        {
            val listRootLayout = attrs.getResourceId(R.styleable.content_item_grid_content_layout, 0)
            if (listRootLayout != 0)
            {
                module.listRootLayout(listRootLayout)
            }

            val listCellLayout = attrs.getResourceId(R.styleable.content_item_grid_grid_cell_layout, 0)
            if (listCellLayout != 0)
            {
                module.listCellLayout(listCellLayout)
            }

            attrs.getString(R.styleable.content_item_grid_orchard_no_elements_message)?.let {
                if (it.isNotEmpty())
                    module.listNoElementsMsg(it)
            }


            val adapterClass = attrs.getString(R.styleable.content_item_grid_content_item_adapter_class)
            if (adapterClass != null && !TextUtils.isEmpty(adapterClass))
            {
                val cls = ClassUtils.fromString<ContentItemAdapter>(adapterClass)
                if (cls != null)
                {
                    module.listAdapterClass(cls)
                }
            }

            val viewHolderClass = attrs.getString(R.styleable.content_item_grid_view_holder_class)
            if (viewHolderClass != null && !TextUtils.isEmpty(viewHolderClass))
            {
                val cls = ClassUtils.fromString<RecyclerView.ViewHolder>(viewHolderClass)
                if (cls != null)
                {
                    module.listViewHolderClass(cls)
                }
            }
        }
    }

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?)
    {
        super.onInflate(context, attrs, savedInstanceState)
        mListMapInflatedModuleCoder.readAttrs(context, attrs)
    }

    override fun onAttach(activity: Context)
    {
        super.onAttach(activity)
        try
        {
            mListener = activity as OnContentItemSelectedListener?
        }
        catch (e: ClassCastException)
        {
            throw ClassCastException("$activity must implement OnContentItemSelectedListener")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mListMapInflatedModuleCoder.writeAttrs(listMapComponentModule)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(listMapComponentModule.listRootLayout, container, false)

        return view
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        recycleView = view.findViewById(android.R.id.list)

        adapter = createNewAdapter()

        adapter.defaultClickReceiver = this
        recycleView.adapter = adapter

        progressBar = view.findViewById(android.R.id.progress)

        if (progressBar != null)
        {
            progressBar?.visibility = View.INVISIBLE
            progressBar?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener
                                                                     {
                                                                         override fun onGlobalLayout()
                                                                         {
                                                                             val progress = progressBar
                                                                             if (progress != null)
                                                                             {
                                                                                 if (progress.measuredHeight == 0 && progress.measuredWidth == 0)
                                                                                     return

                                                                                 progress.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                                                                 // quando il layout viene generato totalmente, si centra la ProgressBar verticalmente nel BottomSheet container
                                                                                 val activity = activity
                                                                                 if (activity != null && activity is ContentItemListMapActivity)
                                                                                 {
                                                                                     val contentItemListMapActivity = activity as ContentItemListMapActivity?
                                                                                     contentItemListMapActivity?.centerViewInBottomSheet(contentItemListMapActivity.gridContainer, progress)
                                                                                 }
                                                                                 if (progress.visibility == View.INVISIBLE)
                                                                                     progress.visibility = View.VISIBLE
                                                                             }
                                                                         }
                                                                     })
        }

        dataConnectionModel.loadingData.observe(viewLifecycleOwner, Observer { aBoolean -> changeProgressVisibility(aBoolean!!) })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        val activity = activity as? ContentItemListMapActivity
        if (activity != null)
        {
            val onScrollListener: RecyclerView.OnScrollListener
            if (activity.isUsingMapAndGridLayout)
            {
                onScrollListener = ListViewPagerSupport(dataConnectionModel, adapter)
            }
            else
            {
                onScrollListener = RefreshableListPagerSupport(dataConnectionModel, adapter, activity)
            }
            recycleView.addOnScrollListener(onScrollListener)
        }
    }

    override fun onDetach()
    {
        super.onDetach()
        mListener = null
    }

    override fun onDataModelChanged(dataModel: DataModel?)
    {
        if (dataModel != null)
        {
            changeBottomSheetBehaviorProperties(false, true)

            val lista = dataModel.listData

            adapter.swapList(lista as List<ContentItem>?, true)

            if (dataModel.listData != null && dataModel.listData.size > 0)
            {
                mNoElementsSnack?.dismiss()

                val page = Math.ceil(1.0 * dataModel.listData.size / orchardComponentModule.pageSize).toInt()

                if (page > dataConnectionModel.page)
                    dataConnectionModel.page = page
            }
            else if (dataModel.cacheValid)
            {
                showNoElementsLayout()
            }
        }
    }

    override fun onDataLoadingError(orchardError: OrchardError)
    {
        if (adapter.itemCount == 0)
        {
            showNoElementsLayout()
        }
    }

    open protected fun showNoElementsLayout()
    {
        val rootView = view
        if (!TextUtils.isEmpty(listMapComponentModule.listNoElementsMsg) && rootView != null)
        {
            mNoElementsSnack = SnackbarUtils.createSnackbar(rootView,
                                                            listMapComponentModule.listNoElementsMsg,
                                                            Snackbar.LENGTH_INDEFINITE)
                    .apply { this.show() }
        }

        changeBottomSheetBehaviorProperties(true, false)
    }

    /**
     * Cambia le proprietà del [BottomSheetBehavior] di questo fragment
     *
     * @param forceModify   true se si vuole forzare la modifica del BottomSheet
     * @param allowUserDrag true se si vuole permettere all'utente di espandere/comprimere il BottomSheet
     */
    protected fun changeBottomSheetBehaviorProperties(forceModify: Boolean, allowUserDrag: Boolean)
    {
        val activity = activity as? ContentItemListMapActivity
        if (activity != null)
        {
            val gridContainer = activity.gridContainer

            if (gridContainer.layoutParams is CoordinatorLayout.LayoutParams)
            {
                val bottomSheetBehavior = (gridContainer.layoutParams as CoordinatorLayout.LayoutParams).behavior as? BottomSheetNotUnderActionBehavior<*>

                if (bottomSheetBehavior != null)
                {
                    bottomSheetBehavior.setAllowUserDrag(allowUserDrag)
                    if (forceModify)
                    {
                        @BottomSheetBehavior.State val state = if (listMapComponentModule.contentPriority == ListMapComponentModule.PRIORITY_LIST)
                            BottomSheetBehavior.STATE_EXPANDED
                        else
                            BottomSheetBehavior.STATE_COLLAPSED

                        bottomSheetBehavior.setStateAndNotify(state)
                    }
                    else
                    {

                        bottomSheetBehavior.setStateAndNotify(bottomSheetBehavior.state)
                    }
                }
            }
        }
    }

    override fun updateDisplayPath(displayPath: String?, reloadImmediately: Boolean)
    {
        dataConnectionModel.orchardModule.displayPath(displayPath)
        if (reloadImmediately && !dataConnectionModel.waitingLogin) {
            dataConnectionModel.restartDataLoading()
        }
    }

    override fun setExtraParameter(key: String, value: String?, reload: Boolean)
    {
        dataConnectionModel.orchardModule.putExtraParameter(key, value)
        if (reload && !dataConnectionModel.waitingLogin)
        {
            dataConnectionModel.restartDataLoading()
        }
    }

    /**
     * Sposta la visualizzazione della lista sull'elemento indicato
     *
     * @param contentItem
     */
    fun scrollAndSelectContentItem(contentItem: ContentItem)
    {
        val index = adapter.indexOf(contentItem)

        adapter.notifyItemChanged(index)
        recycleView.scrollToPosition(index)
    }

    /**
     * Creazione dell'adapter da utilizzare per mostrare i dati all'utente.
     * L'implementazioni di default ritorna un'instanza della classe [ContentItemAdapter]
     *
     * @return il nuovo adapter
     */
    open fun createNewAdapter(): ContentItemAdapter
    {
        val contentItemAdapterClass = listMapComponentModule.listAdapterClass
        val holderClass = listMapComponentModule.listViewHolderClass
        @LayoutRes val cellLayout = listMapComponentModule.listCellLayout
        try
        {
            val constructor = contentItemAdapterClass.getConstructor(Context::class.java,
                                                                     Int::class.javaPrimitiveType,
                                                                     Class::class.java)
            return constructor.newInstance(activity, cellLayout, holderClass) as ContentItemAdapter
        }
        catch (ignored: Exception)
        {
            ignored.printStackTrace()
        }

        return ContentItemAdapter(activity, cellLayout, holderClass)
    }

    override fun onRefresh()
    {
        val mActivity = activity
        if (mActivity != null)
        {
            val mConnection = dataConnectionModel
            mConnection.page = 1
            dataConnectionModel.loadDataFromRemote()
        }
    }

    override fun onViewClicked(recyclerView: RecyclerView, view: View, position: Int, item: ContentItem)
    {
        mListener?.onShowContentItemDetails(this, item)
    }

    protected open fun changeProgressVisibility(isLoading: Boolean)
    {
        if (isLoading && dataConnectionModel.page == 1)
        {
            progressBar?.visibility = View.VISIBLE
        }
        else
        {
            progressBar?.visibility = View.GONE
        }
    }
}