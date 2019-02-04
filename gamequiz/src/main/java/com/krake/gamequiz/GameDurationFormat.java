package com.krake.gamequiz;

import android.content.Context;

import com.krake.gamequiz.R;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * Formatter per indicare quanto tempo manca alla fine del gioco.
 */
public class GameDurationFormat extends NumberFormat {

    private Context mContext;

    public GameDurationFormat(Context context) {

        mContext = context;
    }

    @Override
    public StringBuffer format(double v, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        return format(Long.valueOf(((long) v)), stringBuffer, fieldPosition);
    }

    @Override
    public StringBuffer format(long milliseconds, StringBuffer stringBuffer, FieldPosition fieldPosition) {

        long seconds = milliseconds / 1000;

        long days = milliseconds / (24 * 3600);

        if (days >= 2) {
            stringBuffer.append(String.format(mContext.getString(R.string.days_duration), days));
        } else {
            long hours = seconds / 3600;

            if (hours > 2)
                stringBuffer.append(String.format(mContext.getString(R.string.hours_duration), hours));
            else
                stringBuffer.append(String.format(mContext.getString(R.string.minutes_duration), seconds / 60));
        }

        return stringBuffer;
    }

    @Override
    public Number parse(String s, ParsePosition parsePosition) {
        return null;
    }
}
