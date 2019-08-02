package com.krake.core.address

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import com.krake.core.R
import java.util.*

/**
 * Created by joel on 25/11/16.
 */
class AddressFilterableArrayAdapter(context: Context,
                                    val viewIdentifier: Int = 0,
                                    private val mListener: FilterChangedListener) :
        ArrayAdapter<PlaceResult>(context, R.layout.row_spinner_and_autocomplete, android.R.id.text1), Filterable {

    private var resultList: List<PlaceResult>? = null
    private val userLocationAddress: PlaceResult
    private val googleFakePlace: PlaceResult

    init {
        resultList = ArrayList<PlaceResult>()
        userLocationAddress = PlaceResult.userLocationPlace(context)
        googleFakePlace = PlaceResult("")
    }

    override fun getCount(): Int {
        if (resultList != null && resultList!!.size > 0)
            return resultList!!.size + 2

        return 1
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    override fun isEnabled(position: Int): Boolean {
        if (resultList != null)
            return position != resultList!!.size + 1

        return true
    }

    override fun getItem(position: Int): PlaceResult? {
        if (position > 0 && position < resultList!!.size + 1)
            return resultList!![position - 1]
        else if (position == 0)
            return userLocationAddress
        else
            return googleFakePlace
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView = super.getView(position, convertView, parent)

        val item = getItem(position)
        @DrawableRes val drawable: Int
        var poweredVisibility = View.GONE
        if (position == 0)
            drawable = R.drawable.ic_my_location_24dp
        else if (position < resultList!!.size + 1)
            drawable = R.drawable.ic_place_24dp
        else {
            drawable = 0
            poweredVisibility = View.VISIBLE
        }
        (textView.findViewById<ImageView>(android.R.id.icon)).setImageResource(drawable)

        textView.findViewById<View>(R.id.poweredByGoogle).visibility = poweredVisibility

        (textView.findViewById<TextView>(android.R.id.text2)).apply {
            text = item?.subname

            if (item?.subname.isNullOrEmpty())
                visibility = View.GONE
            else
                visibility = View.VISIBLE
        }

        return textView
    }

    fun setResultList(resultList: List<PlaceResult>?) {
        this.resultList = resultList
    }

    override fun getFilter(): Filter {
        val filter = object : Filter() {
            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val filterResults = Filter.FilterResults()

                mListener.filterChanged(constraint, this@AddressFilterableArrayAdapter)

                if (constraint == null) {
                    resultList = emptyList()
                }

                filterResults.values = resultList
                filterResults.count = resultList!!.size
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
                notifyDataSetChanged()

            }
        }
        return filter
    }

    interface FilterChangedListener {
        fun filterChanged(constraint: CharSequence?, adapter: AddressFilterableArrayAdapter)
    }
}