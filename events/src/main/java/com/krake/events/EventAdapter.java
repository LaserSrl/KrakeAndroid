package com.krake.events;

import android.content.Context;
import android.view.View;

import com.krake.core.widget.ContentItemAdapter;
import com.krake.core.widget.ImageTextCellHolder;
import com.krake.events.model.Event;

import java.text.DateFormat;

/**
 * Created by joel on 21/12/15.
 */
public class EventAdapter extends ContentItemAdapter {

    private DateFormat completeDateFormat;

    public EventAdapter(Context context, int layout, Class holderClass) {
        super(context, layout, holderClass);
        completeDateFormat = DateFormat.getDateInstance(context.getResources().getInteger(R.integer.event_date_format));
    }

    @Override
    public void onBindViewHolder(ImageTextCellHolder holder, int i) {
        super.onBindViewHolder(holder, i);

        final EventViewHolder eventViewHolder = (EventViewHolder) holder;
        final Event event = (Event) getItem(i);

        if (event == null || event.getActivityPart() == null) {
            eventViewHolder.datesContainer.setVisibility(View.GONE);
        } else {
            eventViewHolder.beginDateView.setText(completeDateFormat.format(event.getActivityPart().getDateTimeStart()));
            eventViewHolder.endDateView.setText(completeDateFormat.format(event.getActivityPart().getDateTimeEnd()));
            eventViewHolder.datesContainer.setVisibility(View.VISIBLE);
        }
    }
}
