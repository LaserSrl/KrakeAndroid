package com.krake.core.cache

import android.content.Context
import android.location.Location
import android.support.v4.util.ArrayMap
import android.util.Log
import com.krake.core.Constants
import com.krake.core.R
import com.krake.core.app.KrakeApplication
import com.krake.core.component.module.OrchardComponentModule
import com.krake.core.model.RequestCache
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Default implementation of [CacheManager] used in [KrakeApplication] to add automatically
 * the query parameters to the paths in cache.
 *
 * @param context application [Context].
 */
class AutoQueryCacheManager(context: Context) : CacheManager, LocationCacheModifier {
    var defaultCacheValidity: Int = context.resources.getInteger(R.integer.default_cache_validity)

    private val gameRandQuery = context.getString(R.string.orchard_query_game_rand)
    private val userInfoAlias: String = context.getString(R.string.orchard_user_info_display_path)

    private val cacheForPaths: ArrayMap<String, Int> by lazy {
        val map = ArrayMap<String, Int>()
        map.put(userInfoAlias, 10)
        map
    }

    private val paramsToIgnore: MutableList<String> by lazy {
        mutableListOf(Constants.REQUEST_DISPLAY_PATH_KEY,
                      Constants.REQUEST_COMPLEX_BEHAVIOUR,
                      Constants.REQUEST_DEEP_LEVEL,
                      Constants.REQUEST_REAL_FORMAT,
                      Constants.REQUEST_PAGE_KEY,
                      Constants.REQUEST_PAGE_SIZE_KEY,
                      Constants.REQUEST_ITEM_PART_FILTER,
                      Constants.REQUEST_LATITUDE,
                      Constants.REQUEST_LONGITUDE,
                      Constants.REQUEST_RADIUS,
                      Constants.REQUEST_LANGUAGE_KEY)
    }

    private val paramsForModelKey: MutableList<String> by lazy {
        mutableListOf(
                Constants.REQUEST_RESULT_TARGET
        )
    }

    private val aroundMePaths: MutableList<String> by lazy {
        mutableListOf<String>()
    }

    /**
     * Change the cache validity related to a path.
     *
     * @param path the path added in cache.
     * @param validity the time expressed in minutes after which the cache will be invalidated.
     */
    override fun setCacheValidity(path: String, validity: Int) {
        cacheForPaths.put(path, validity)
    }

    /**
     * Add a query parameter that will be ignored when all parameters will be added
     * automatically to the path.
     */
    fun addParamToIgnore(param: String): AutoQueryCacheManager = apply {
        paramsToIgnore.add(param)
    }

    fun addKeyForModelIdentification(key: String): AutoQueryCacheManager = apply {
        paramsForModelKey.add(key)
    }



    override fun isCacheValid(cache: RequestCache, extras: Map<String, String>): Boolean {
        val cacheExtras = cache.extras
        val gameRandVal = extras[gameRandQuery]
        if (!gameRandVal.isNullOrEmpty() && gameRandVal != cacheExtras[gameRandQuery])
            return false

        val cacheName = if (extras.containsKey(Constants.REQUEST_DISPLAY_PATH_KEY))
            extras[Constants.REQUEST_DISPLAY_PATH_KEY]
        else
            cache.cacheName

        if (aroundMePaths.contains(cache.cacheName)) {
            val requestRadius = extras[Constants.REQUEST_RADIUS]?.toInt() ?: 0
            val cacheRadius = cacheExtras[Constants.REQUEST_RADIUS]?.toInt() ?: 0

            if (requestRadius == cacheRadius) {
                val requestLocation = Location("")
                requestLocation.latitude = extras[Constants.REQUEST_LATITUDE]?.toDouble() ?: 0.0
                requestLocation.longitude = extras[Constants.REQUEST_LONGITUDE]?.toDouble() ?: 0.0

                val cacheLocation = Location("")
                cacheLocation.latitude = cacheExtras[Constants.REQUEST_LATITUDE]?.toDouble() ?: 0.0
                cacheLocation.longitude = cacheExtras[Constants.REQUEST_LONGITUDE]?.toDouble() ?: 0.0

                if (requestLocation.distanceTo(cacheLocation) > requestRadius / 3)
                    return false
            } else {
                return false
            }
        }

        val validity = if (cacheForPaths.containsKey(cacheName)) cacheForPaths[cacheName]!! else defaultCacheValidity

        val lastValidCache = Date(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(validity.toLong()))

        return !cache.dateExecuted.before(lastValidCache)
    }

    override fun getModelKey(orchardModule: OrchardComponentModule): String
    {
        var modelKey: String = ""

        if (orchardModule.displayPath != null)
        {
            if (orchardModule.dataClass != null)
                modelKey = orchardModule.dataClass.toString() + orchardModule.displayPath!!
            else
                modelKey = orchardModule.displayPath!!
        }
        else if (orchardModule.dataClass != null)
        {
            if (orchardModule.recordStringIdentifier != null)
                modelKey = orchardModule.dataClass.toString() + orchardModule.recordStringIdentifier
            else if (orchardModule.recordIdentifier != null)
                modelKey = orchardModule.dataClass.toString() + orchardModule.recordIdentifier
            else
                modelKey = orchardModule.dataClass.toString()
        }
        else
        {
            Log.e("Cache key", "Richiesta una cache con parametri non validi!")
        }

        for (key in paramsForModelKey)
        {
            val extra = orchardModule.extraParameters[key]

            if (extra != null)
            {
                modelKey = modelKey + extra
            }
        }

        return modelKey
    }

    override fun getCacheKey(orchardModule: OrchardComponentModule): String
    {
        return getCacheKey(orchardModule.displayPath!!, orchardModule.extraParameters)
    }

    override fun getCacheKey(displayPath: String, extras: Map<String, String>): String {
        val sb = StringBuilder(displayPath)
        if (displayPath != userInfoAlias) {
            val keys = extras.keys.toMutableList()
            // Sort keys to manage the bundle.putAll() re-order.
            Collections.sort(keys)

            for (key in keys) {
                if (!paramsToIgnore.contains(key)) {
                    sb.append(key)
                    sb.append(':')
                    sb.append(extras[key])
                    sb.append(';')
                }
            }
        }
        return sb.toString()
    }

    override fun addLocationPath(path: String) {
        aroundMePaths.add(path)
    }
}