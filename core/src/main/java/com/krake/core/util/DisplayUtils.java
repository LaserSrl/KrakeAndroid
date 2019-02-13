package com.krake.core.util;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;
import androidx.annotation.NonNull;

/**
 * Classe di utils per il Display
 */
public class DisplayUtils {
    /**
     * Stabilisce se il dispositivo è in portrait o landscape
     *
     * @param context context utilizzato dal WindowManager
     * @return true se in portrait, false se in landscape
     */
    public static boolean isPortrait(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x < size.y;
    }
}
