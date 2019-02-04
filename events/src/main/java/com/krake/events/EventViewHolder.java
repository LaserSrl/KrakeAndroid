package com.krake.events;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.krake.core.widget.ImageTextCellHolder;

/**
 * Created by joel on 21/12/15.
 */
public class EventViewHolder extends ImageTextCellHolder {
    public ViewGroup datesContainer;
    public TextView beginDateView;
    public TextView endDateView;

    public EventViewHolder(View itemView) {
        super(itemView);

        datesContainer = itemView.findViewById(R.id.eventDatesContainer);
        beginDateView = itemView.findViewById(R.id.eventBeginDateTextView);
        endDateView = itemView.findViewById(R.id.eventEndDateTextView);
    }
}