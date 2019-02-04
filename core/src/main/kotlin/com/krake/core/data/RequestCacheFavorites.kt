package com.krake.core.data

import com.krake.core.ClassUtils
import com.krake.core.model.RecordWithIdentifier
import com.krake.core.model.RecordWithStringIdentifier
import com.krake.core.model.RequestCache
import io.realm.Realm
import io.realm.RealmModel
import java.util.*

/**
 * Crea una cache con gli elementi indicati. Se presente usa la cache già creata e ne modifica il contenuto
 * @param name nome della cache
 * @param elements elementi
 */
fun RequestCache.Companion.createCache(name: String, elements: List<RealmModel>) {
    var cache = findCacheWith(name)
    Realm.getDefaultInstance().beginTransaction()
    if (cache != null) {
        cache.clearAllLists()
    } else {
        cache = newRequestCache(name)
    }

    cache?.addAll(elements)

    Realm.getDefaultInstance().commitTransaction()
}

private fun newRequestCache(name: String): RequestCache? {
    var cache1 = ClassUtils.instantiateObjectOfDataClass(RequestCache::class.simpleName) as RequestCache
    cache1.cacheName = name
    cache1.dateExecuted = Date(Long.MAX_VALUE)
    cache1 = Realm.getDefaultInstance().copyToRealmOrUpdate(cache1)
    return cache1
}

/**
 * Verifica se un elemento è in una lista.
 * Se la lista non è presente non viene creata
 * @param listName nome della cache
 * @param element elemento da inserire
 * @return true se la cache esiste e l'elemento esite, false altrimenti
 */
fun RequestCache.Companion.isInFavorite(listName: String, element: RealmModel): Boolean {
    val cache = findCacheWith(listName)

    if (cache != null) {
        return cache.containsFavorite(element)
    }
    return false
}

fun RequestCache.containsFavorite(element: RealmModel): Boolean {

    if (element is RecordWithIdentifier) {
        return this.elements(element.javaClass).find { (it as RecordWithIdentifier).identifier == element.identifier } != null
    } else if (element is RecordWithStringIdentifier) {
        return this.elements(element.javaClass).find { (it as RecordWithStringIdentifier).stringIdentifier == element.stringIdentifier } != null
    }
    return false
}

/**
 * Aggiunge un elemento alla cache
 */
fun RequestCache.Companion.addToFavorite(listName: String, element: RealmModel) {
    var cache = findCacheWith(listName)

    val realm = Realm.getDefaultInstance()
    realm.beginTransaction()



    if (cache == null)
        cache = newRequestCache(listName)

    cache!!.add(realm.copyToRealmOrUpdate(element))

    realm.commitTransaction()
}

/**
 * Rimuove un elemento dalla cache
 */
fun RequestCache.Companion.removeFromFavorite(listName: String, element: RealmModel) {
    val cache = findCacheWith(listName)



    if (cache != null) {

        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()

        cache.remove(realm.copyToRealmOrUpdate(element))

        realm.commitTransaction()
    }
}

/**
 * Toglie o aggiunge un elemento dalla cache
 */
fun RequestCache.Companion.toggleFromFavorite(listName: String, element: RealmModel): Boolean {

    val wasInFavorites = isInFavorite(listName, element)

    if (wasInFavorites)
        removeFromFavorite(listName, element)
    else
        addToFavorite(listName, element)

    return !wasInFavorites
}
