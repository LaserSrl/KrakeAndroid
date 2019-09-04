package com.krake.core.model

import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.krake.core.ClassUtils
import com.krake.core.StringUtils
import com.krake.core.data.RemoteDataRepository
import com.krake.core.getProperty
import com.krake.core.util.realmCleanClassName
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmModel
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by joel on 01/03/17.
 */

interface RequestCache : RealmModel {
    var _extras: String
    var cacheName: String
    var dateExecuted: Date

    var extras: HashMap<String, String>
        get() {
            if (!TextUtils.isEmpty(_extras))
                return Gson().fromJson<HashMap<String, String>>(_extras, object : TypeToken<HashMap<String, String>>() {

                }.type)
            else
                return HashMap()
        }
        set(value) {
            _extras = Gson().toJson(value)
        }

    var _sort: String
    private var sortIdentifiers: ArrayList<String>
        get() {
            if (!TextUtils.isEmpty(_sort))
                return Gson().fromJson<ArrayList<String>>(_sort, object : TypeToken<ArrayList<String>>() {

                }.type)
            else
                return ArrayList()
        }
        set(value) {
            _sort = Gson().toJson(value)
        }

    fun elements(clazz: Class<out RealmModel>?): List<RealmModel> {
        if (clazz != null) {
            val list = realmList(clazz)
            if (list != null)
                return list
            else
                return LinkedList()
        }

        val allElements = LinkedList<RealmModel>()
        cachedElementsClassNames.forEach {
            @Suppress("UNCHECKED_CAST")
            (getProperty(it) as? RealmList<RealmModel>)?.let {
                allElements.addAll(it)
            }
        }

        val sort = sortIdentifiers
        allElements.sortBy { sort.indexOf(it.identifierOrStringIdentifier) }

        return allElements
    }

    fun clearAllLists() {
        cachedElementsClassNames.forEach { @Suppress("UNCHECKED_CAST") (getProperty(it) as? RealmList<RealmModel>)?.clear() }
        _sort = ""
    }

    fun add(item: RealmModel) {
        realmList(item.javaClass)?.add(item)
        val sort = sortIdentifiers
        sort.add(item.identifierOrStringIdentifier)
        sortIdentifiers = sort
    }

    fun addAll(elements: Collection<RealmModel>) {
        val classes = elements.distinctBy { it::class }

        if (classes.size == 1) {
            realmList(elements.first().javaClass)?.addAll(elements)

            val sort = sortIdentifiers
            elements.forEach { sort.add(it.identifierOrStringIdentifier) }
            sortIdentifiers = sort
        } else {
            elements.forEach { add(it) }
        }
    }

    fun remove(item: RealmModel) {
        realmList(item.javaClass)?.remove(item)
        val sort = sortIdentifiers
        sort.remove(item.identifierOrStringIdentifier)
        sortIdentifiers = sort
    }

    fun removeAll(elements: Collection<RealmModel>) {
        val classes = elements.distinctBy { it::class }

        if (classes.size == 1) {
            realmList(elements.first().javaClass)?.removeAll(elements)
            val sort = sortIdentifiers
            elements.forEach { sort.remove(it.identifierOrStringIdentifier) }
            sortIdentifiers = sort
        } else {
            elements.forEach { remove(it) }
        }
    }

    private fun realmList(forClass: Class<out RealmModel>): RealmList<RealmModel>? {

        val name = StringUtils.stringWithFirstLetterLowercase(forClass.simpleName.realmCleanClassName())
        @Suppress("UNCHECKED_CAST")
        return getProperty(name) as? RealmList<RealmModel>
    }

    companion object {
        val cachedElementsClassNames: MutableList<String> by lazy { RemoteDataRepository.shared.dataMapper.configurations.getGeneratedClassFields("RequestCache")!! }

        fun findCacheWith(name: String): RequestCache? {
            val cacheClass: Class<RealmModel> = ClassUtils.dataClassForName(RequestCache::class.simpleName)
            return Realm.getDefaultInstance()
                    .where(cacheClass)
                    .equalTo("cacheName", name)
                    .findFirst() as? RequestCache
        }
    }

}