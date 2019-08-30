package com.krake.contentcreation

import android.view.View
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.JsonArray
import com.krake.core.OrchardError
import com.krake.core.PrivacyViewModel
import com.krake.core.component.module.LoginComponentModule
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.data.DataConnectionModel
import com.krake.core.data.DataModel
import com.krake.core.model.ContentItem
import java.util.*

class MultiEnumOrTermManager constructor(context: FragmentActivity,
                                         callingFragment: Fragment,
                                         private val fieldInfo: ContentCreationTabInfo.FieldInfo,
                                         layout: View,
                                         private val mListener: MultiSelectionListener,
                                         private val adaptListViewHeight: Boolean,
                                         possibleValues: JsonArray?,
                                         selectedValues: JsonArray?) : AdapterView.OnItemClickListener
{

    private val listView: ListView = layout.findViewById(android.R.id.list)
    private val selectedValues: MutableList<Any>
    private val mAdapter: EnumOrTermPartAdapter
    private val progress: ProgressBar = layout.findViewById(android.R.id.progress)
    val errorView = layout.findViewWithTag<TextView>("Error")
    private var mDataConnection: DataConnectionModel? = null
    private val modelObserver = object : Observer<DataModel>
    {
        override fun onChanged(t: DataModel?)
        {
            if (t != null && t.cacheValid)
            {

                val values = t.listData.filter { it is ContentItem }
                        .map { SelectableContentItem(it as ContentItem) }
                progress.visibility = View.GONE
                setValuesAndSelection(values)
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
                stopConnection()
            }
        }
    }


    init {
        val titleText = layout.findViewById<TextView>(R.id.fieldTitleText)
        titleText.setText(fieldInfo.name)

        mAdapter = EnumOrTermPartAdapter(context, callingFragment, fieldInfo.isEditingEnabled, R.layout.multi_selection_enum_term_row, true, LinkedList())
        listView.choiceMode = AbsListView.CHOICE_MODE_MULTIPLE
        listView.adapter = mAdapter

        if (fieldInfo.isEditingEnabled)
            listView.onItemClickListener = this

        this.selectedValues = {
            val selection = ArrayList<Any>()

            selectedValues?.forEach {
                it.unwrappedValue()?.let {
                    selection.add(it)
                }
            }
            selection
        }()

        if (possibleValues != null) {
            val selectableValues = possibleValues.unwrapToSelectableValue()

            val selectableUnwrappedValues = selectableValues.map { selectableValue -> selectableValue.value }

            val nonValidValues = this.selectedValues.filterNot {
                selectableUnwrappedValues.contains(it)
            }

            nonValidValues.forEach {
                this.selectedValues.remove(it)
            }

            setValuesAndSelection(selectableValues)
        } else {

            val orchardModule = OrchardComponentModule()
                    .displayPath(fieldInfo.orchardComponentModule)

            val dataConnection = DataConnectionModel(orchardModule, LoginComponentModule(), ViewModelProviders.of(context).get(PrivacyViewModel::class.java))

            progress.visibility = View.VISIBLE
            mDataConnection = dataConnection

            dataConnection.model.observe(context, modelObserver)
            dataConnection.dataError.observe(context, errorObserver)
        }
    }

    private fun setValuesAndSelection(allValues: List<SelectableValue>) {
        mAdapter.allValues = allValues

        val lp = listView.layoutParams
        var totalHeight = listView.paddingTop + listView.paddingBottom
        val adapter = listView.adapter
        if (adaptListViewHeight) {
            for (i in 0 until allValues.size) {
                val listItem = adapter.getView(i, null, listView)
                listItem.measure(0, 0)
                totalHeight += listItem.measuredHeight
            }
            lp.height = totalHeight + listView.dividerHeight * (adapter.count - 1)
        }

        listView.layoutParams = lp


        for (selectedValue in this.selectedValues) {
            var position: Int

            position = 0
            while (position < mAdapter.count) {
                val wrap = mAdapter.allValues[position]

                if (wrap.value == selectedValue) {
                    break
                }
                ++position
            }

            if (position < mAdapter.count)
                listView.setItemChecked(position, true)
        }

        if (adaptListViewHeight) {
            var parent = listView.parent

            do {
                if (parent is NestedScrollView) {
                    parent.smoothScrollTo(0, 0)
                    break;
                }
                parent = parent.parent

            } while (parent != null)
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val flags = listView.checkedItemPositions
        val selected = flags.get(position)

        listView.setItemChecked(position, selected)

        mAdapter.allValues[position]
                .value?.let {
            if (!selected) {
                selectedValues.remove(it)
            } else {
                selectedValues.add(it)
            }
        }

        mListener.onTermOrEnumSelected(this, selectedValues.toJsonArray())
        mAdapter.notifyDataSetChanged()
    }

    val fieldKey: String
        get() = fieldInfo.orchardKey


    private fun stopConnection()
    {
        mDataConnection?.let {
            it.model.removeObserver(modelObserver)
            it.dataError.removeObserver(errorObserver)
        }
        mDataConnection = null
    }

    interface MultiSelectionListener {
        fun onTermOrEnumSelected(spinnerManager: MultiEnumOrTermManager, selectedValues: JsonArray)
    }
}
