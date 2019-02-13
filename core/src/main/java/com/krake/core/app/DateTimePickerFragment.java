package com.krake.core.app;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.krake.core.R;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * DialogFragment che permette di selezionare una data ed eventualmente un'ora.
 * L'activity che integra il fragment deve implementare l'interfaccia {@link com.krake.core.app.DateTimePickerFragment.OnDateTimePickerListener}
 */
public class DateTimePickerFragment extends DialogFragment implements DialogInterface.OnClickListener, TabLayout.OnTabSelectedListener {

    /**
     * Long: the number of milliseconds since Jan. 1, 1970, midnight GMT.
     */
    public static final String ARG_END_DATE = "endDate";
    public static final String ARG_START_DATE = "startDate";
    public static final String ARG_MIN_DATE = "minDate";
    public static final String ARG_MAX_DATE = "maxDate";

    /**
     * Bool: se abilitare anche la selezione dell'ora
     */
    public static final String ARG_ENABLE_TIME_PICKER = "enableTimePicker";
    /**
     * Bool: se abilitare anche la selezione della data di fine
     */
    public static final String ARG_ENABLE_END_DATE = "enableEndDate";
    private static final Integer TAB_FROM_TAG = 12;
    private static final Integer TAB_TO_TAG = 13;

    private static final String STATE_SELECTED_TAB_INDEX = "TabIndex";

    protected Date mStartDate;
    protected Date mEndDate;

    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private TabLayout mTabLayout;
    private Date minDate;
    private Date maxDate;

    private OnDateTimePickerListener mListener;

    private boolean mEnableTimePicker;
    private boolean mEnableEndDate;

    public DateTimePickerFragment() {
        // Required empty public constructor
    }

    /**
     * Crea una nuova istanza del fragment
     *
     * @param startDate             data la mostrare all'inizio del fragment
     * @param enableTimePicker indicazione se è necessario mostrare anche il TimePicker
     * @return
     */
    public static DateTimePickerFragment newInstance(@NonNull Date startDate, boolean enableTimePicker) {
        return newInstance(startDate, null, null, null, enableTimePicker, false);
    }

    public static DateTimePickerFragment newInstance(@NonNull Date startDate, @Nullable Date minDate, @Nullable Date maxDate) {
        return newInstance(startDate, null, minDate, maxDate, false, false);
    }

    public static DateTimePickerFragment newInstance(@NonNull Date startDate, @NonNull Date endDate) {
        return newInstance(startDate, endDate, null, null, false, true);
    }

    public static DateTimePickerFragment newInstance(@NonNull Date startDate, @NonNull Date endDate, @Nullable Date minDate, @Nullable Date maxDate) {
        return newInstance(startDate, endDate, minDate, maxDate, false, true);
    }

    private static DateTimePickerFragment newInstance(@NonNull Date startDate, @Nullable Date endDate,
                                                      @Nullable Date minDate, @Nullable Date maxDate,
                                                      boolean enableTimePicker, boolean enableEndDate) {
        DateTimePickerFragment fragment = new DateTimePickerFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_START_DATE, startDate.getTime());
        if (endDate != null)
            args.putLong(ARG_END_DATE, endDate.getTime());

        if (minDate != null)
            args.putLong(ARG_MIN_DATE, minDate.getTime());

        if (maxDate != null)
            args.putLong(ARG_MAX_DATE, maxDate.getTime());

        args.putBoolean(ARG_ENABLE_TIME_PICKER, enableTimePicker);
        args.putBoolean(ARG_ENABLE_END_DATE, enableEndDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        onAttachCommon(activity);
    }

    protected void onAttachCommon(Context context) {
        try {
            mListener = (OnDateTimePickerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + OnDateTimePickerListener.class.getSimpleName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStartDate = new Date(getArguments().getLong(ARG_START_DATE));
            if (getArguments().containsKey(ARG_END_DATE))
                mEndDate = new Date(getArguments().getLong(ARG_END_DATE));
            mEnableTimePicker = getArguments().getBoolean(ARG_ENABLE_TIME_PICKER);
            mEnableEndDate = getArguments().getBoolean(ARG_ENABLE_END_DATE);

            if (getArguments().containsKey(ARG_MIN_DATE))
                minDate = new Date(getArguments().getLong(ARG_MIN_DATE));

            if (getArguments().containsKey(ARG_MAX_DATE))
                maxDate = new Date(getArguments().getLong(ARG_MAX_DATE));

        }
    }

    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setView(createView(savedInstanceState));

        alert.setNegativeButton(android.R.string.cancel, null);
        alert.setPositiveButton(android.R.string.ok, this);

        return alert.create();
    }


    private View createView(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Inflate the layout for this fragment
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.fragment_date_time_picker, null, false);

        mTabLayout = view.findViewById(android.R.id.tabs);

        if (mEnableEndDate) {
            mTabLayout.addTab(mTabLayout.newTab().setText(R.string.from).setTag(TAB_FROM_TAG));
            mTabLayout.addTab(mTabLayout.newTab().setText(R.string.to).setTag(TAB_TO_TAG));

            if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SELECTED_TAB_INDEX)) {
                mTabLayout.getTabAt(savedInstanceState.getInt(STATE_SELECTED_TAB_INDEX)).select();
            }

            mTabLayout.setOnTabSelectedListener(this);
        } else {
            mTabLayout.setVisibility(View.GONE);
        }

        mDatePicker = view.findViewById(R.id.datePicker);

        mTimePicker = view.findViewById(R.id.timePicker);
        mTimePicker.setIs24HourView(true);

        try {
            mDatePicker.getCalendarView().setShowWeekNumber(false);
        } catch (Exception ignored) {

        }

        mTimePicker = view.findViewById(R.id.timePicker);
        if(!mEnableTimePicker)
            mTimePicker.setVisibility(View.GONE);

        updateUIWithSelectedDate(!mEnableEndDate || mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition()).getTag().equals(TAB_FROM_TAG));

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEnableEndDate) {
            outState.putInt(STATE_SELECTED_TAB_INDEX, mTabLayout.getSelectedTabPosition());
        }
    }

    private void updateUIWithSelectedDate(boolean showStartDate) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(showStartDate ? mStartDate.getTime() : mEndDate.getTime());

        long minTime;
        if (showStartDate) {
            if (minDate != null)
                minTime = minDate.getTime();
            else
                minTime = 0;
        }
        else
            minTime = mStartDate.getTime();

        mDatePicker.setMinDate(minTime);
        if (maxDate != null)
            mDatePicker.setMaxDate(maxDate.getTime());

        mDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        if (mEnableTimePicker) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mTimePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
                mTimePicker.setMinute(calendar.get(Calendar.MINUTE));
            } else {
                //noinspection deprecation
                mTimePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
                //noinspection deprecation
                mTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        updateDateFromSelectedTab(!mEnableEndDate || mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition()).getTag().equals(TAB_FROM_TAG));
        if (mListener != null) {
            mListener.onDatePicked(mStartDate, mEndDate);
        }
    }

    private void updateDateFromSelectedTab(boolean startDate) {
        if (!startDate) {
            mEndDate = getSelectedDate();
        } else {
            long endDateDiff = 0;
            if (mEndDate != null) {
                endDateDiff = mEndDate.getTime() - mStartDate.getTime();
                if (endDateDiff < 0) {
                    endDateDiff = 0;
                }
            }

            mStartDate = getSelectedDate();
            if (mEndDate != null) {
                mEndDate = new Date(mStartDate.getTime() + endDateDiff);
            }
        }
    }

    @NonNull
    private Date getSelectedDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        if (!mEnableTimePicker) {
            calendar.set(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth(), 0, 0, 0);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                calendar.set(mDatePicker.getYear(),
                        mDatePicker.getMonth(),
                        mDatePicker.getDayOfMonth(),
                        mTimePicker.getHour(),
                        mTimePicker.getMinute(),
                        0);
            } else {
                //noinspection deprecation
                calendar.set(mDatePicker.getYear(),
                        mDatePicker.getMonth(),
                        mDatePicker.getDayOfMonth(),
                        mTimePicker.getCurrentHour(),
                        mTimePicker.getCurrentMinute(),
                        0);
            }
        }
        calendar.add(Calendar.SECOND, 1);
        return calendar.getTime();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        updateDateFromSelectedTab(tab.getTag().equals(TAB_TO_TAG));

        updateUIWithSelectedDate(tab.getTag().equals(TAB_FROM_TAG));
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    /**
     * Interfaccia che devono implementare le activity per poter comunicare col Fragment
     */
    public interface OnDateTimePickerListener {

        /**
         * Informazione che l'utente ha selezionato una nuova data.
         *
         * @param startDate data selezionata dall'utente
         */
        void onDatePicked(@NonNull Date startDate, @Nullable Date endDate);
    }

}
