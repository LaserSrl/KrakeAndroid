package com.krake.core.text;

import android.content.Context;

import com.krake.core.R;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * Format per la gestione della duarat di un viaggio.
 * Viene indicata come hh ore, mm min.
 */
public class DurationFormat extends NumberFormat {

    private static DurationFormat mSharedFormat;
    private Context mContext;

    public DurationFormat(Context context) {
        mContext = context;
    }

    public static DurationFormat getInstance(Context context) {
        if (mSharedFormat == null || mSharedFormat.mContext != context) {
            mSharedFormat = new DurationFormat(context);
        }
        return mSharedFormat;
    }

    @Override
    public StringBuffer format(double v, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        return format((long) Math.ceil(v), stringBuffer, fieldPosition);
    }

    @Override
    public StringBuffer format(long seconds, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        long hours = seconds / 3600;

        seconds -= hours * 3600;

        long minutes = seconds / 60;

        if (hours > 0) {
            stringBuffer.append(hours);
            stringBuffer.append(' ');
            stringBuffer.append(mContext.getString(R.string.hours));
        }

        if (minutes > 0) {
            if (hours > 0) {
                stringBuffer.append(" ");
            }
            stringBuffer.append(minutes);
            stringBuffer.append(' ');
            stringBuffer.append(mContext.getString(R.string.minutes));
        }

        return stringBuffer;
    }

    @Override
    public Number parse(String s, ParsePosition parsePosition) {
        return null;
    }
}
