package com.krake.events;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.Html;
import android.text.TextUtils;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import com.krake.core.app.ContentItemDetailModelFragment;
import com.krake.core.data.DataModel;
import com.krake.core.model.ActivityPart;
import com.krake.core.model.ContentItemWithDescription;
import com.krake.core.model.ContentItemWithLocation;
import com.krake.core.model.MapPart;
import com.krake.core.permission.PermissionManager;
import com.krake.events.component.module.EventComponentModule;
import com.krake.events.model.Event;
import kotlin.collections.ArraysKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Classe per mostrare i dettagli di un evento
 * Created by joel on 20/01/15.
 */
public class EventDetailsFragment extends ContentItemDetailModelFragment {
    protected DateFormat mCompleteDateFormat;
    private Event mEvent;
    private TextView mBeginDateView;
    private TextView mEndDateView;
    private ViewGroup mDatesContainer;
    private PermissionManager mPermissionManager;

    @Override
    protected int getContentLayoutIdentifier() {
        @LayoutRes int defaultLayout = super.getContentLayoutIdentifier();
        return defaultLayout == R.layout.fragment_base_content_item ? EventComponentModule.Companion.getDEFAULT_DETAIL_CONTENT_LAYOUT() : defaultLayout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (mCompleteDateFormat == null) {
            mCompleteDateFormat = DateFormat.getDateInstance(getActivity().getResources().getInteger(R.integer.event_date_format));
        }

        mPermissionManager = new PermissionManager(this)
                .permissions(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                .addListener(this);

        mPermissionManager.create();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatesContainer = view.findViewById(R.id.eventDatesContainer);
        if (mDatesContainer != null) {
            mBeginDateView = view.findViewById(R.id.eventBeginDateTextView);
            mEndDateView = view.findViewById(R.id.eventEndDateTextView);

            int padding = getResources().getDimensionPixelSize(R.dimen.content_details_internal_padding);
            mDatesContainer.setPadding(padding, 0, padding, 0);
            mDatesContainer.setBackgroundResource(R.color.details_background_color);
        }
    }

    @Override
    public void onDataModelChanged(@Nullable DataModel dataModel) {
        super.onDataModelChanged(dataModel);

        if (dataModel != null && dataModel.getListData().size() > 0) {
            mEvent = (Event) dataModel.getListData().get(0);

            ActivityPart activityPart = mEvent.getActivityPart();

            boolean activityPartIsValid = false;

            if (activityPart != null) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.invalidateOptionsMenu();
                }

                Date startDate = activityPart.getDateTimeStart();
                Date endDate = activityPart.getDateTimeEnd();
                if (startDate != null && endDate != null) {
                    if (mBeginDateView != null) {
                        mBeginDateView.setText(mCompleteDateFormat.format(startDate));
                    }
                    if (mEndDateView != null) {
                        mEndDateView.setText(mCompleteDateFormat.format(endDate));
                    }
                    activityPartIsValid = true;
                }
            }

            if (mDatesContainer != null) {
                if (activityPartIsValid) {
                    mDatesContainer.setVisibility(View.VISIBLE);
                } else {
                    mDatesContainer.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_save_date, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_save_the_date).setVisible(mEvent != null &&
                mEvent.getActivityPart() != null &&
                mEvent.getActivityPart().getDateTimeStart() != null &&
                mEvent.getActivityPart().getDateTimeEnd() != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save_the_date) {
            mPermissionManager.request();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onPermissionsHandled(@NotNull String[] acceptedPermissions) {
        super.onPermissionsHandled(acceptedPermissions);
        if (ArraysKt.contains(acceptedPermissions, Manifest.permission.READ_CALENDAR) && ArraysKt.contains(acceptedPermissions, Manifest.permission.WRITE_CALENDAR)) {
            saveTheDateContentValues(createSaveTheDateIntent());
        }
    }

    @SuppressWarnings("MissingPermission")
    @RequiresPermission(allOf = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR})
    private void saveTheDateContentValues(ContentValues values) {
        ContentResolver cr = getActivity().getContentResolver();

        String selection = "(("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND (" + CalendarContract.Calendars.OWNER_ACCOUNT + " NOT LIKE '%#%'))";
        String[] selectionArgs = new String[]{"com.google"};

        Cursor cur = cr.query(CalendarContract.Calendars.CONTENT_URI, new String[]{
                CalendarContract.Calendars._ID,                           // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.CALENDAR_TIME_ZONE, CalendarContract.Calendars.OWNER_ACCOUNT}, selection, selectionArgs, CalendarContract.Calendars.DEFAULT_SORT_ORDER);

        Uri insertUri = null;
        if (cur != null && cur.moveToFirst()) {
            values.put(CalendarContract.Events.CALENDAR_ID, cur.getLong(cur.getColumnIndex(CalendarContract.Calendars._ID)));
            values.put(CalendarContract.Events.EVENT_TIMEZONE, cur.getLong(cur.getColumnIndex(CalendarContract.Calendars.CALENDAR_TIME_ZONE)));

            insertUri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

            cur.close();
        }

        boolean showErrorDialog;
        if (insertUri != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, insertUri);
            try {
                startActivity(intent);
                showErrorDialog = false;
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                showErrorDialog = true;
            }
        } else {
            showErrorDialog = true;
        }

        if (showErrorDialog) {
            new AlertDialog.Builder(getActivity()).setMessage(getString(R.string.error_inserting_event_in_calendar))
                    .setNeutralButton(android.R.string.ok, null)
                    .show();
        }
    }

    protected ContentValues createSaveTheDateIntent() {
        ContentValues values = new ContentValues();

        Calendar beginCalendar = Calendar.getInstance();
        //noinspection ConstantConditions
        beginCalendar.setTime(mEvent.getActivityPart().getDateTimeStart());

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(mEvent.getActivityPart().getDateTimeEnd());

        values.put(CalendarContract.Events.TITLE, mEvent.getTitlePartTitle());

        // remove HTML tags if the item has a body part
        if (mEvent instanceof ContentItemWithDescription) {
            String originalDescription = ((ContentItemWithDescription) mEvent).getBodyPartText();
            if (!TextUtils.isEmpty(originalDescription)) {
                String description;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    description = Html.fromHtml(originalDescription, Html.FROM_HTML_MODE_LEGACY).toString();
                else {
                    //noinspection deprecation
                    description = Html.fromHtml(originalDescription).toString();
                }

                values.put(CalendarContract.Events.DESCRIPTION, description);
            }
        }

        if (mEvent instanceof ContentItemWithLocation) {
            MapPart eventMapPart = ((ContentItemWithLocation) mEvent).getMapPart();
            if (eventMapPart != null && !TextUtils.isEmpty(eventMapPart.getLocationAddress()))
                values.put(CalendarContract.Events.EVENT_LOCATION, eventMapPart.getLocationAddress());
        }

        if (beginCalendar.get(Calendar.DAY_OF_YEAR) != endCalendar.get(Calendar.DAY_OF_YEAR) ||
                beginCalendar.get(Calendar.YEAR) != endCalendar.get(Calendar.YEAR)) {

            //noinspection ConstantConditions
            if (mEvent.getActivityPart().getDateTimeStart().before(new Date()))
                beginCalendar.setTime(new Date());
            beginCalendar.set(Calendar.HOUR_OF_DAY, 0);
            beginCalendar.set(Calendar.MINUTE, 0);
            endCalendar.set(Calendar.HOUR_OF_DAY, 0);
            endCalendar.set(Calendar.MINUTE, 0);

            values.put(CalendarContract.Events.DTSTART, beginCalendar.getTimeInMillis());
            values.put(CalendarContract.Events.DTEND, endCalendar.getTimeInMillis());
            values.put(CalendarContract.Events.ALL_DAY, 1); //true


            values.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
        } else {
            if (beginCalendar.getTimeInMillis() == endCalendar.getTimeInMillis()) {
                endCalendar.set(Calendar.HOUR_OF_DAY, endCalendar.get(Calendar.HOUR_OF_DAY) + 1);
            }

            values.put(CalendarContract.Events.DTSTART, beginCalendar.getTimeInMillis());
            values.put(CalendarContract.Events.DTEND, endCalendar.getTimeInMillis());

            values.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        }

        return values;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPermissionManager.removeListener(this);
    }
}