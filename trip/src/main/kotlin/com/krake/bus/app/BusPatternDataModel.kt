package com.krake.bus.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.krake.bus.model.BusPassage
import com.krake.bus.model.BusPattern
import com.krake.core.data.DataConnectionModel
import com.krake.core.data.DataModel
import io.realm.RealmModel
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class BusPatternDataModel : DataConnectionModel()
{
    val busPatternModel: LiveData<DataModel> = Transformations.map(model) { source ->
        if (source != null)
        {
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
                    val departure: Long?

                    if (time.realtimeDeparture != null && time.realtimeDeparture != 0L)
                        departure = time.realtimeDeparture
                    else
                        departure = time.scheduledDeparture

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
                        passage.pattern = pattern
                        passages.add(passage)
                    }
                }
            }

            if (passages.isNotEmpty())
            {
                Collections.sort(passages) { lhs, rhs -> lhs.passage!!.compareTo(rhs.passage) }
            }

            DataModel(passages, source.cacheValid);
        }
        else
        {
            DataModel(LinkedList<RealmModel>(), false);
        }
    }
}