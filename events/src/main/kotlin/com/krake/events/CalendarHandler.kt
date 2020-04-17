package com.krake.events

import android.Manifest
import android.content.*
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import android.text.Html
import android.text.TextUtils
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import com.krake.core.model.ContentItemWithDescription
import com.krake.core.model.ContentItemWithLocation
import com.krake.events.model.Event
import java.util.*

object CalendarHandler {

    @RequiresPermission(allOf = [Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR])
    fun saveEventInCalendar(context: Context, event: Event) {
        val values = createCalendarContentValues(event)
        saveEventInCalendar(context, values)
    }

    @RequiresPermission(allOf = [Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR])
    fun saveEventInCalendar(context: Context, values: ContentValues) {
        val cr: ContentResolver = context.contentResolver
        val selection = ("((" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND (" + CalendarContract.Calendars.OWNER_ACCOUNT + " NOT LIKE '%#%'))")
        val selectionArgs = arrayOf("com.google")
        val cur = cr.query(
            CalendarContract.Calendars.CONTENT_URI, arrayOf(
                CalendarContract.Calendars._ID,  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.CALENDAR_TIME_ZONE,
                CalendarContract.Calendars.OWNER_ACCOUNT
            ), selection, selectionArgs, CalendarContract.Calendars.DEFAULT_SORT_ORDER
        )
        var insertUri: Uri? = null
        if (cur != null && cur.moveToFirst()) {
            values.put(
                CalendarContract.Events.CALENDAR_ID, cur.getLong(
                    cur.getColumnIndex(
                        CalendarContract.Calendars._ID
                    )
                )
            )
            values.put(
                CalendarContract.Events.EVENT_TIMEZONE, cur.getLong(
                    cur.getColumnIndex(
                        CalendarContract.Calendars.CALENDAR_TIME_ZONE
                    )
                )
            )
            insertUri = cr.insert(CalendarContract.Events.CONTENT_URI, values)
            cur.close()
        }
        val showErrorDialog: Boolean
        showErrorDialog = if (insertUri != null) {
            val intent = Intent(Intent.ACTION_VIEW, insertUri)
            try {
                context.startActivity(intent)
                false
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                true
            }
        } else {
            true
        }
        if (showErrorDialog) {
            AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.error_inserting_event_in_calendar))
                .setNeutralButton(android.R.string.ok, null)
                .show()
        }
    }

    fun createCalendarContentValues(event: Event): ContentValues {
        val values = ContentValues()
        var beginCalendar = Calendar.getInstance()
        beginCalendar.time = event.activityPart!!.dateTimeStart!!
        var endCalendar = Calendar.getInstance()
        endCalendar.time = event.activityPart!!.dateTimeEnd!!
        values.put(CalendarContract.Events.TITLE, event.titlePartTitle)
        // remove HTML tags if the item has a body part
        if (event is ContentItemWithDescription) {
            val originalDescription =
                (event as ContentItemWithDescription).bodyPartText
            if (!TextUtils.isEmpty(originalDescription)) {
                val description: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(
                    originalDescription,
                    Html.FROM_HTML_MODE_LEGACY
                ).toString() else {
                    Html.fromHtml(originalDescription).toString()
                }
                values.put(CalendarContract.Events.DESCRIPTION, description)
            }
        }
        if (event is ContentItemWithLocation) {
            val eventMapPart =
                (event as ContentItemWithLocation).mapPart
            if (eventMapPart != null && !TextUtils.isEmpty(eventMapPart.locationAddress)) values.put(
                CalendarContract.Events.EVENT_LOCATION, eventMapPart.locationAddress
            )
        }
        if (beginCalendar[Calendar.DAY_OF_YEAR] != endCalendar[Calendar.DAY_OF_YEAR] ||
            beginCalendar[Calendar.YEAR] != endCalendar[Calendar.YEAR]
        ) {
            val originalStart = beginCalendar
            val originalEnd = endCalendar
            beginCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            endCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            if (event.activityPart!!.dateTimeStart!!.before(Date())) {
                beginCalendar.time = Date()
            } else {
                beginCalendar[Calendar.YEAR] = originalStart[Calendar.YEAR]
                beginCalendar[Calendar.MONTH] = originalStart[Calendar.MONTH]
                beginCalendar[Calendar.DAY_OF_MONTH] = originalStart[Calendar.DAY_OF_MONTH]
            }
            endCalendar[Calendar.HOUR_OF_DAY] = 0
            endCalendar[Calendar.YEAR] = originalEnd[Calendar.YEAR]
            endCalendar[Calendar.MONTH] = originalEnd[Calendar.MONTH]
            endCalendar[Calendar.DAY_OF_MONTH] = originalEnd[Calendar.DAY_OF_MONTH] + 1
            values.put(CalendarContract.Events.DTSTART, beginCalendar.timeInMillis)
            values.put(CalendarContract.Events.DTEND, endCalendar.timeInMillis)
            values.put(CalendarContract.Events.ALL_DAY, 1) //true
            values.put(
                CalendarContract.Events.AVAILABILITY,
                CalendarContract.Events.AVAILABILITY_FREE
            )
        } else {
            if (beginCalendar.timeInMillis == endCalendar.timeInMillis) {
                endCalendar[Calendar.HOUR_OF_DAY] = endCalendar[Calendar.HOUR_OF_DAY] + 1
            }
            values.put(CalendarContract.Events.DTSTART, beginCalendar.timeInMillis)
            values.put(CalendarContract.Events.DTEND, endCalendar.timeInMillis)
            values.put(
                CalendarContract.Events.AVAILABILITY,
                CalendarContract.Events.AVAILABILITY_BUSY
            )
        }
        return values
    }

}