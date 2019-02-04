package com.krake.usercontent;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.krake.core.model.EnumerationField;

/**
 * Classe per impostare lo stile della label dello stato di un contenuto creato.
 */
public class StatusLabelUtils {
    public static void setStatusLabel(TextView mStatusTextView, boolean loginRequired, EnumerationField value) {
        Context context = mStatusTextView.getContext();
        if (loginRequired) {
            mStatusTextView.setVisibility(View.VISIBLE);

            String statusText;
            int drawableBackground;
            switch (value.getValue()) {
                case "Created":
                    statusText = context.getString(R.string.status_waiting);
                    drawableBackground = R.drawable.status_waiting_background;
                    break;

                case "Rejected":
                    statusText = context.getString(R.string.status_rejected);

                    drawableBackground = R.drawable.status_rejected_background;
                    break;

                default:
                case "Loaded":
                case "Accepted":
                    statusText = context.getString(R.string.status_accepted);
                    drawableBackground = R.drawable.status_accepted_background;
                    break;
            }

            mStatusTextView.setText(statusText);
            mStatusTextView.setBackgroundResource(drawableBackground);
        } else {
            mStatusTextView.setVisibility(View.GONE);
        }
    }
}
