package com.krake.core.app

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import com.krake.core.ClassUtils
import com.krake.core.OrchardError
import com.krake.core.PrivacyViewModel
import com.krake.core.R
import com.krake.core.cache.CacheManager
import com.krake.core.component.annotation.BundleResolvable
import com.krake.core.component.base.ComponentManager
import com.krake.core.component.base.ComponentModule
import com.krake.core.component.module.LoginComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.data.DataConnectionModel
import com.krake.core.data.DataModel

abstract class OrchardDataModelFragment : Fragment()
{
    @BundleResolvable
    lateinit var internalOrchardComponentModule: OrchardComponentModule
    @BundleResolvable
    lateinit var internalLoginComponentModule: LoginComponentModule

    protected var mDataLoadListener: DataConnectionLoadListener? = null
    lateinit var dataConnectionModel: DataConnectionModel
        private set

    var closeListener: DetailFragmentCloseInterface? = null
        private set

    private val mOrchardInflatedModuleCoder: FragmentInflatedModuleCoder<OrchardComponentModule> = object : FragmentInflatedModuleCoder<OrchardComponentModule>(R.styleable.orchard_data_fragment)
    {
        /**
         * Permette di copiare gli attributi risolti in precedenza in un [ComponentModule]
         * Non c'è bisogno di richiamare il metodo [TypedArray.recycle] perché verrà richiamato automaticamente.
         *
         * @param attrs  attributi salvati in precedenza.
         * @param module [ComponentModule] sul quale verranno copiati gli attributi.
         */
        public override fun copyToModule(attrs: TypedArray, module: OrchardComponentModule)
        {
            val className = attrs.getString(R.styleable.orchard_data_fragment_orchard_data_class)
            if (!TextUtils.isEmpty(className))
            {
                val cls = ClassUtils.dataClassForName(className)
                if (cls != null)
                {
                    module.dataClass(cls)
                }
            }

            val displayPath = attrs.getString(R.styleable.orchard_data_fragment_orchard_display_path)
            if (!TextUtils.isEmpty(displayPath))
            {
                module.displayPath(displayPath)
            }

            val contentItemFilters = attrs.getString(R.styleable.orchard_data_fragment_item_attributes_filter)
            if (!TextUtils.isEmpty(contentItemFilters))
            {
                module.dataPartFilters(contentItemFilters)
            }

            val pageSize = attrs.getInt(R.styleable.orchard_data_fragment_orchard_page_size, 0)
            if (pageSize != 0)
            {
                module.pageSize(pageSize)
            }

            val deepLevel = attrs.getInt(R.styleable.orchard_data_fragment_deep_level, 0)
            if (deepLevel != 0)
            {
                module.deepLevel(deepLevel)
            }
        }
    }

    val orchardComponentModule
        get() = dataConnectionModel.orchardModule
    val loginComponentModule
        get() = dataConnectionModel.loginModule

    override fun onInflate(context: Context?, attrs: AttributeSet?, savedInstanceState: Bundle?)
    {
        super.onInflate(context, attrs, savedInstanceState)
        if (attrs != null)
            mOrchardInflatedModuleCoder.readAttrs(context!!, attrs)
    }

    override fun onAttach(activity: Context?)
    {
        super.onAttach(activity)

        if (activity is DetailFragmentCloseInterface)
            closeListener = activity

        if (activity is DataConnectionLoadListener)
            mDataLoadListener = activity
    }

    open fun needToAccessDataInMultiThread(): Boolean
    {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        ComponentManager.resolveArguments(this)

        mOrchardInflatedModuleCoder.writeAttrs(internalOrchardComponentModule)
        dataConnectionModel = ViewModelProviders.of(activity!!)
                .get(CacheManager.shared.getModelKey(internalOrchardComponentModule),
                     internalOrchardComponentModule.dataConnectionModelClass)
        onConfigureModel()

        dataConnectionModel.dataError.observe(this, Observer { orchardError ->
            if (orchardError != null)
            {
                onDataLoadingError(orchardError)
                val model: DataModel?

                if (!needToAccessDataInMultiThread())
                    model = dataConnectionModel.model.value
                else
                    model = dataConnectionModel.multiThreadModel.value

                mDataLoadListener?.onDataLoadFailed(orchardError, model)
            }
        })

        dataConnectionModel.loadingData.observe(this, Observer { loading ->
            if (loading != null)
            {
                isLoadingData(loading)
                mDataLoadListener?.onDataLoading(loading, dataConnectionModel.page)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        observeDataModel()
    }

    protected open fun observeDataModel()
    {
        val model: LiveData<DataModel>

        if (!needToAccessDataInMultiThread())
            model = dataConnectionModel.model
        else
            model = dataConnectionModel.multiThreadModel

        model.observe(this, Observer { dataModel -> onDataModelChanged(dataModel) })
    }

    private fun isLoadingData(loading: Boolean)
    {
    }

    abstract fun onDataModelChanged(dataModel: DataModel?)

    abstract fun onDataLoadingError(orchardError: OrchardError)

    open protected fun onConfigureModel()
    {
        dataConnectionModel.configure(internalOrchardComponentModule,
                                      internalLoginComponentModule,
                                      ViewModelProviders.of(activity!!).get(PrivacyViewModel::class.java))
    }

    override fun onDetach()
    {
        super.onDetach()
        closeListener = null
        mDataLoadListener = null
    }
}