package com.krake.contentcreation

import android.view.View
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonArray
import com.krake.core.OrchardError
import com.krake.core.PrivacyViewModel
import com.krake.core.component.module.LoginComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.data.DataConnectionModel
import com.krake.core.data.DataModel
import com.krake.core.model.ContentItem
import java.util.*

class EnumOrTermSpinnerManager
/**
 * Il manager si occupa di gestire il caricamento e la visulizzazione della categoria.
 * Nel categoryLayout è necessario che siano presenti 2 view:
 *
 *  1. uno [Spinner] con id R.id.categorySpinner dove saranno mostrate le categorie
 *
 *
 *  1. uno [TextView] con id R.id.fieldTitleText dove è indicato il nome del campo
 *

 * @param context        context della chiamata
 * *
 * @param fieldInfo      informazioni di creazione del field
 * *
 * @param categoryLayout View che contiene le parti di layout gestite dal category manager
 * *
 * @param mListener      listener per quando viene selezionata una galleria
 *
 * @param possibleValues all the values for the selection. This value can be null only if the field info type in ContentPicker
 */
constructor(private val context: FragmentActivity,
            callingFragment: Fragment,
            private val fieldInfo: ContentCreationTabInfo.FieldInfo,
            categoryLayout: View,
            private val mListener: SelectionListener,
            possibleValues: JsonArray?,
            selectedValue: JsonArray?) : AdapterView.OnItemSelectedListener
{


    protected var mCategorySpinner: Spinner = categoryLayout.findViewById(R.id.categorySpinner)
    var selectedValue: Any? = null
        private set

    private val mAdapter: EnumOrTermPartAdapter
    private val progress: ProgressBar = categoryLayout.findViewById(android.R.id.progress)

    private var mDataConnection: DataConnectionModel? = null

    private val modelObserver = object : Observer<DataModel>
    {
        override fun onChanged(t: DataModel?)
        {
            if (t != null && t.cacheValid)
            {

                val values = t.listData.filter { it is ContentItem }
                        .map { SelectableContentItem(it as ContentItem) }

                setValuesAndSelection(values)
                progress.visibility = View.GONE
                mCategorySpinner.visibility = View.VISIBLE
                stopConnection()
            }
        }
    }

    private val errorObserver = object : Observer<OrchardError>
    {
        override fun onChanged(t: OrchardError?)
        {
            if (t != null)
            {
                progress.visibility = View.GONE
                mCategorySpinner.visibility = View.VISIBLE
                stopConnection()
            }
        }
    }

    init {
        val titleText = categoryLayout.findViewById<TextView>(R.id.fieldTitleText)

        titleText.setText(fieldInfo.name)

        mAdapter = EnumOrTermPartAdapter(context, callingFragment, fieldInfo.isEditingEnabled, R.layout.single_selection_enum_term_row, false, LinkedList())
        mCategorySpinner.adapter = mAdapter
        mCategorySpinner.onItemSelectedListener = this
        mCategorySpinner.isEnabled = false

        this.selectedValue = {
            val primitive = selectedValue?.firstOrNull()?.asJsonPrimitive

            if (primitive?.isString == true)
                primitive.asString
            else
                primitive?.asLong
        }()

        if (fieldInfo.orchardComponentModule == null) {
            setValuesAndSelection(
                    possibleValues!!.unwrapToSelectableValue())
        } else {

            val orchardModule = OrchardComponentModule()
                    .displayPath(fieldInfo.orchardComponentModule)

            val loginModule = LoginComponentModule()
                    .loginRequired(fieldInfo.isLoginEnabled)

            val dataConnection = DataConnectionModel(orchardModule,
                                                     loginModule,
                                                     ViewModelProvider(context).get(PrivacyViewModel::class.java))

            progress.visibility = View.VISIBLE
            mCategorySpinner.visibility = View.GONE
            mDataConnection = dataConnection
            dataConnection.model.observe(context, modelObserver)
            dataConnection.dataError.observe(context, errorObserver)
        }
    }

    private fun setValuesAndSelection(allValues: List<SelectableValue>) {
        mAdapter.allValues = allValues

        if (selectedValue == null)
            selectedValue = initialSelectedValue()

        var index = 0
        for (wrapped in mAdapter.allValues) {

            if (wrapped.value == this.selectedValue) {
                mCategorySpinner.setSelection(index)
                break
            }
            ++index
        }

        mCategorySpinner.isEnabled = mAdapter.allValues.size > 1 && fieldInfo.isEditingEnabled

        selectedValue?.let {
            mListener.onSingleTermOrEnumSelected(this, it)
        }
    }

    private fun initialSelectedValue(): Any? {

        for (index in 0 until mAdapter.count) {
            val value = mAdapter.allValues[index].value

            if (value != null)
                return value

        }
        return null
    }

    protected val fieldKey: String
        get() = fieldInfo.orchardKey

    protected val serverObjectToCreate: JsonArray
        get() {
            val array = JsonArray()
            val currentValue = selectedValue

            if (currentValue is String)
                array.add(currentValue)
            else if (currentValue is Number)
                array.add(currentValue)

            return array
        }

    override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        val newValue = mAdapter.getItem(i)?.value
        if (selectedValue !== newValue) {
            selectedValue = newValue
            selectedValue?.let { mListener.onSingleTermOrEnumSelected(this, it) }
        }
    }

    override fun onNothingSelected(adapterView: AdapterView<*>)
    {

    }

    private fun stopConnection()
    {
        mDataConnection?.let {
            it.model.removeObserver(modelObserver)
            it.dataError.removeObserver(errorObserver)
        }
        mDataConnection = null
    }

    interface SelectionListener {
        fun onSingleTermOrEnumSelected(spinnerManager: EnumOrTermSpinnerManager, selectedValue: Any)
    }

}