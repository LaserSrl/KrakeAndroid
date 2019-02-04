package com.krake.cards

import android.content.Context
import android.text.TextUtils
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.krake.core.contacts.ContactInfo
import java.util.*

/**
 * Adapter di [ContactInfo] implementa interfaccia [Filterable] per permette di
 * filtrare i contatti in base allìinput digitato dall'utente.
 */
class FilterableContactInfoAdapter(context: Context, private val allContacts: List<ContactInfo>) :
        ArrayAdapter<ContactInfo>(context, android.R.layout.simple_list_item_1, android.R.id.text1), Filterable {

    private val resultList: LinkedList<ContactInfo> = LinkedList()

    override fun getCount(): Int {
        return resultList.size
    }

    override fun getItem(position: Int): ContactInfo? {
        return resultList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val filterResults = FilterResults()

                resultList.clear()

                if (!TextUtils.isEmpty(constraint)) {
                    val lowerCaseConstraint = constraint.toString().toLowerCase(Locale.getDefault())
                    allContacts.filterTo(resultList) {
                        it.name?.toLowerCase(Locale.getDefault())?.contains(lowerCaseConstraint) ?: false
                    }
                }

                filterResults.values = resultList

                filterResults.count = resultList.size
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }
}