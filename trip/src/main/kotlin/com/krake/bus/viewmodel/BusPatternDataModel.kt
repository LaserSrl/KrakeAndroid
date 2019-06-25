package com.krake.bus.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.krake.OtpDataRepository
import com.krake.bus.model.BusPassage
import com.krake.bus.model.BusPattern
import com.krake.bus.model.BusRoute
import com.krake.core.data.DataConnectionModel
import com.krake.core.data.DataModel
import com.krake.core.model.identifierOrStringIdentifier
import com.krake.core.thread.AsyncTask
import com.krake.core.thread.async
import io.realm.Realm
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class BusPatternDataModel : DataConnectionModel()
{
    private var busRoutesTask: AsyncTask<List<BusRoute>>? = null
    private val mutableBusRoutes = MutableLiveData<List<BusRoute>>()
    val busRoutes: LiveData<List<BusRoute>> = mutableBusRoutes

    private val mutableStatus = MutableLiveData<Status>()
    val status: LiveData<Status> = mutableStatus

    private val busPatternMutableModel = MediatorLiveData<DataModel>().apply {
        addSource(model, ::transformPatterns)
        addSource(busRoutes, ::transformRoutes)
    }
    val busPatternModel: LiveData<DataModel> = busPatternMutableModel

    init {
        mutableStatus.value = Idle
        loadBusRoutes()
    }

    private fun transformPatterns(source: DataModel?) {
        mergePatterns(source, busRoutes.value)
    }

    private fun transformRoutes(source: List<BusRoute>?) {
        mergePatterns(model.value, source)
    }

    private fun mergePatterns(source: DataModel?, routes: List<BusRoute>?) {
        if (source == null) {
            busPatternMutableModel.value = DataModel(LinkedList(), false)
            return
        }

        val realm = Realm.getDefaultInstance()
        val passages = ArrayList<BusPassage>()

        val now = Date()
        val calendar = Calendar.getInstance()
        calendar.time = now
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val midNight = calendar.time

        val twoHoursLater = Date(now.time + 120 * TimeUnit.MINUTES.toMillis(1))

        val fromRegexPattern = Pattern.compile("^([\\s\\S]+) to ([\\s\\S]+) from ([\\s\\S]+)$")
        val onlyToRegex = Pattern.compile("^([\\s\\S]+) to ([\\s\\S]+)$")

        @Suppress("UNCHECKED_CAST")
        for (pattern in (source.listData as List<BusPattern>))
        {
            for (time in pattern.stopTimes)
            {
                val departure = if (time.realtimeDeparture != null && time.realtimeDeparture != 0L)
                    time.realtimeDeparture
                else
                    time.scheduledDeparture

                val passageDate = Date(midNight.time + departure!! * 1000)

                if (now.before(passageDate) && twoHoursLater.after(passageDate))
                {
                    val passage = BusPassage()
                    passage.passage = passageDate
                    var matcher = fromRegexPattern.matcher(pattern.descriptionText!!)

                    if (!matcher.matches())
                        matcher = onlyToRegex.matcher(pattern.descriptionText!!)

                    if (matcher.matches())
                    {
                        passage.lineNumber = matcher.group(1)

                        var name = matcher.group(2)
                        val index = name.lastIndexOf("(")
                        if (index != -1)
                        {
                            name = name.substring(0, index).trim { it <= ' ' }
                        }

                        passage.destination = name
                    }
                    passage.lastStop = time.isLastStop()
                    passage.pattern = realm.copyFromRealm(pattern)
                    passage.tripId = time.tripId

                    if (passage.pattern?.busRoute == null) {
                        val routeId = passage.pattern?.identifierOrStringIdentifier?.split(":")?.let { "${it[0]}:${it[1]}" }
                        passage.pattern?.busRoute = routes?.find { it.identifierOrStringIdentifier == routeId }
                    }

                    passages.add(passage)
                }
            }
        }

        if (passages.isNotEmpty())
        {
            passages.sortWith(Comparator { lhs, rhs -> lhs.passage!!.compareTo(rhs.passage) })
        }


        val distinc = passages.distinctBy {
            var uniqueCode = ""
            uniqueCode += it.destination
            uniqueCode += it.lineNumber
            uniqueCode += it.tripId

            uniqueCode
        }

        busPatternMutableModel.value = DataModel(distinc, source.cacheValid)
    }

    fun loadBusRoutes() {
        mutableStatus.value = Loading

        busRoutesTask = async {
            OtpDataRepository.shared.loadBusRoutes()
        }.completed {
            mutableBusRoutes.value = it
            mutableStatus.value = Idle
        }.error {
            mutableStatus.value = Error
        }.build()
        busRoutesTask?.load()
    }

    override fun onCleared() {
        super.onCleared()
        busRoutesTask?.cancel()
    }
}