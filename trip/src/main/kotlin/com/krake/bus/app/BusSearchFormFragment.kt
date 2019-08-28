package com.krake.bus.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.fragment.app.Fragment
import com.krake.core.address.AddressFilterableArrayAdapter
import com.krake.core.address.PlaceResult
import com.krake.core.text.DistanceNumberFormat
import com.krake.core.util.LayoutUtils
import com.krake.trip.R

/**
 * Created by antoniolig on 28/04/2017.
 */
class BusSearchFormFragment : Fragment(), AddressFilterableArrayAdapter.FilterChangedListener,
    AdapterView.OnItemClickListener,
    SeekBar.OnSeekBarChangeListener,
    AdapterView.OnItemSelectedListener {


    companion object {
        private val TAG = BusSearchFormFragment::class.java.simpleName
        private const val PREFS_BUS_SEARCH = "prefsBusSearch"
        private const val PREFS_KEY_RADIUS = "keyRadius"
        private const val ARG_INITIAL_ADDRESS_NAME = "argInitAddressName"

        fun newInstance(initialAddressName: String? = null): BusSearchFormFragment {
            val fragment = BusSearchFormFragment()
            initialAddressName?.let {
                val args = Bundle()
                args.putString(ARG_INITIAL_ADDRESS_NAME, it)
                fragment.arguments = args
            }
            return fragment
        }
    }

    private lateinit var addressAutocomplete: AutoCompleteTextView
    private lateinit var seekBar: SeekBar
    private lateinit var distanceTextView: TextView
    private var listener: Listener? = null
    private lateinit var searchModeTab: Spinner

    private var minimumDistance: Int = 0
    var radius: Int = 0
        get() = seekBar.progress + minimumDistance
        private set

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = context as? Listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.partial_bus_search_layout, container, false)

        val prefs = context!!.getSharedPreferences(PREFS_BUS_SEARCH, Context.MODE_PRIVATE)

        addressAutocomplete = view.findViewById(R.id.searchAddressEditText)
        seekBar = view.findViewById(R.id.distanceSeekBar)
        distanceTextView = view.findViewById(R.id.distanceTextView)
        searchModeTab = view.findViewById(R.id.busSearchModeTab)

        searchModeTab.onItemSelectedListener = this
        searchModeTab.adapter = ArrayAdapter(
            activity,
            android.R.layout.simple_list_item_1, android.R.id.text1,
            arrayOf(getString(R.string.address), getString(R.string.bus_stop_name))
        )

        addressAutocomplete.setAdapter(AddressFilterableArrayAdapter(context!!, 0, this))
        addressAutocomplete.onItemClickListener = this

        minimumDistance = resources.getInteger(R.integer.distance_search_mininum_radius)
        val maxDistance = resources.getInteger(R.integer.distance_search_maximum_radius)

        seekBar.max = maxDistance - minimumDistance
        seekBar.progress = prefs.getInt(PREFS_KEY_RADIUS, (maxDistance - minimumDistance) / 2)
        distanceTextView.text = DistanceNumberFormat.sharedInstance.formatDistance(radius.toFloat())
        seekBar.setOnSeekBarChangeListener(this)

        (arguments?.getString(ARG_INITIAL_ADDRESS_NAME))?.let {
            setAddress(it)
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                Log.d(TAG, "Search form height: ${view.measuredHeight}")
                listener?.onSearchFormLayoutReady()
            }
        })

        return view
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val context = context ?: throw IllegalArgumentException("The context mustn't be null.")
        LayoutUtils.hideKeyboard(context, addressAutocomplete)
        addressAutocomplete.clearFocus()
        (parent?.adapter?.getItem(position) as? PlaceResult)?.let {
            listener?.onPlaceResultChosen(it)
        }
    }

    override fun filterChanged(constraint: CharSequence?, adapter: AddressFilterableArrayAdapter) {
        (constraint?.toString())?.let {
            listener?.onAddressTextChange(it)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        // Unused method.
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        distanceTextView.text = DistanceNumberFormat.sharedInstance.formatDistance(radius.toFloat())
        listener?.onRadiusChange(false)
    }

    @SuppressLint("ApplySharedPref")
    override fun onStopTrackingTouch(seekBar: SeekBar) {
        val context = context ?: throw IllegalArgumentException("The context mustn't be null.")
        val prefs = context.getSharedPreferences(PREFS_BUS_SEARCH, Context.MODE_PRIVATE)
        prefs.edit().putInt(PREFS_KEY_RADIUS, seekBar.progress).commit()
        listener?.onRadiusChange(true)
    }

    fun setAddress(addressName: String?) {
        addressAutocomplete.setText(addressName)
        addressAutocomplete.clearFocus()
    }

    fun setPlaceResultList(places: List<PlaceResult>) {
        val adapter = addressAutocomplete.adapter as AddressFilterableArrayAdapter
        adapter.setResultList(places)
        adapter.notifyDataSetChanged()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

        listener?.changeAddressSearchMode(if (p2 == 0) AddressSearchMode.Google else AddressSearchMode.Otp)

        if (addressAutocomplete.text.length > 0) {
            listener?.onAddressTextChange(addressAutocomplete.text.toString())
        }
    }

    interface Listener {
        fun onSearchFormLayoutReady()
        fun onRadiusChange(finished: Boolean)
        fun onAddressTextChange(addressName: String)
        fun onPlaceResultChosen(placeResult: PlaceResult)
        fun changeAddressSearchMode(mode: AddressSearchMode)
    }

    enum class AddressSearchMode {
        Google, Otp
    }
}