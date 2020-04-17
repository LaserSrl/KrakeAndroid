package com.krake.events;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import com.krake.core.app.ContentItemDetailModelFragment;
import com.krake.core.data.DataModel;
import com.krake.core.model.ActivityPart;
import com.krake.core.permission.PermissionManager;
import com.krake.events.component.module.EventComponentModule;
import com.krake.events.model.Event;
import kotlin.Deprecated;
import kotlin.collections.ArraysKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.text.DateFormat;
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
            CalendarHandler.INSTANCE.saveEventInCalendar(getActivity(), createSaveTheDateIntent());
        }
    }

    @Deprecated(message = "Use CalendarHandler.createCalendarContentValues instead")
    protected ContentValues createSaveTheDateIntent() {
        return CalendarHandler.INSTANCE.createCalendarContentValues(mEvent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPermissionManager.removeListener(this);
    }
}