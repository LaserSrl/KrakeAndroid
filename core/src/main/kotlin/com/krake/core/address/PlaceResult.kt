package com.krake.core.address

import android.content.Context
import android.location.Location
import android.support.annotation.StyleRes
import android.text.TextUtils
import android.text.style.TextAppearanceSpan
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.krake.core.R
import com.krake.core.model.ContentItemWithLocation

/**
 * Classe per rappresentare un posto in cui fare una richiesta di viagfio
 * @property placeId Identificato del placeId (usato solo se la palce è creata a partire dalla richiesta di Place di Google)
 * @property location posizione dell'utente, può esserre null se mancano ancora i dati. Logicamente è possibile solo per la posizione dell'utente
 * se non è stata identificata. Oppure se è un dato incompleto recuperato dalla richiesta di Plce
 */
open class PlaceResult {
    val placeId: String?
    val isUserLocation: Boolean
    var name: String
        protected set
    @StyleRes var theme: Int = R.style.AppTheme
    var location: Location? = null
    var geocodeAddress: Boolean = false
        private set

    constructor(context: Context, prediction: AutocompletePrediction) {
        this.name = prediction.getPrimaryText(TextAppearanceSpan(context, R.style.ActionBar_TitleText)).toString()
        placeId = prediction.placeId
        isUserLocation = false
    }

    constructor(contentItem: ContentItemWithLocation) : this(contentItem.titlePartTitle ?: "", false) {
        location = Location("")

        val mapPart = contentItem.mapPart
        if (mapPart != null && mapPart.isMapValid) {
            location?.latitude = mapPart.latitude
            location?.longitude = mapPart.longitude

            if (TextUtils.isEmpty(mapPart.locationAddress)) {
                geocodeAddress = true
            } else {
                name = mapPart.locationAddress!!
            }
        }
    }

    val latLng: LatLng? get() {
        val location = this.location
        if (location != null) {
            return LatLng(location.latitude, location.longitude)
        }
        return null
    }

    constructor(name: String) : this(name, false)

    protected constructor(name: String, isUserLocation: Boolean) {
        this.name = name
        placeId = null
        this.isUserLocation = isUserLocation
    }

    fun setGeocodedName(name: String) {
        this.name = name
        geocodeAddress = false
    }

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (other is PlaceResult) {
            return this.name == other.name && (this.latLng?.equals(other.latLng) ?: false)
        }
        return super.equals(other)
    }

    companion object {

        private var userLocationTripPlace: PlaceResult? = null

        /**
         * Indirizzo speciale rappresenta la posizione dell'utente

         * @param context context dell'App
         * *
         * @return istanza globale della posizione dell'utente
         */
        fun userLocationPlace(context: Context): PlaceResult {
            if (userLocationTripPlace != null)
                return userLocationTripPlace!!

            userLocationTripPlace = PlaceResult(context.getString(R.string.user_location), true)
            return userLocationTripPlace!!
        }
    }
}