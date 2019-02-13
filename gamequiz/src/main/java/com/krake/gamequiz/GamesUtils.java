package com.krake.gamequiz;

import android.app.Activity;
import android.app.Dialog;
import android.content.IntentSender;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.snackbar.Snackbar;

/**
 * Created by joel on 26/06/15.
 */
public class GamesUtils {
    static public boolean resolveConnectionFailure(Activity activity,
                                                   GoogleApiClient client,
                                                   ConnectionResult result, int requestCode,
                                                   int fallbackErrorMessage) {

        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(activity, requestCode);
                return true;
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                client.connect();
                return false;
            }
        } else {
            // not resolvable... so show an error message
            int errorCode = result.getErrorCode();
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                    activity, requestCode);
            if (dialog != null) {
                dialog.show();
            } else {
                // no built-in dialog: show the fallback error message
                showAlert(activity, fallbackErrorMessage);
            }
            return false;
        }
    }

    static private void showAlert(Activity activity, int message) {
        Snackbar.make(activity.findViewById(R.id.activity_layout_coordinator), message, Snackbar.LENGTH_LONG).show();
    }
}
