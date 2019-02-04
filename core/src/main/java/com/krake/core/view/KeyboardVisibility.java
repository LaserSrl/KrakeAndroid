package com.krake.core.view;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;

import com.krake.core.R;

/**
 * Created by joel on 24/09/13.
 */
public class KeyboardVisibility {
    public static boolean shouldUpdateUserInterfaceWithKeyboardVisible(Activity activity) {
        if (!activity.getResources().getBoolean(R.bool.is_tablet)) {
            View view = activity.findViewById(android.R.id.content);
            Rect r = new Rect();
            view.getWindowVisibleDisplayFrame(r);

            int statusBatHeight = 0;
            int resourceId = activity.getResources().getIdentifier(
                    "status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBatHeight = activity.getResources().getDimensionPixelSize(
                        resourceId);
            }

            int heightDiff = view.getRootView().getHeight()
                    - view.getHeight();

            heightDiff = heightDiff - (activity.getActionBar().getHeight() + statusBatHeight);

            return heightDiff > 160;
        }

        return false;
    }
}
