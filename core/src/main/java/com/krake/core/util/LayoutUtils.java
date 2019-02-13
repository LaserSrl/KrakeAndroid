package com.krake.core.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.appbar.AppBarLayout;

/**
 * Classe di utils per il layout
 */
public class LayoutUtils {
    private LayoutUtils() {
        // empty private constructor
    }

    /**
     * Nasconde la tastiera se presente
     *
     * @param context context corrente
     * @param view    view dalla quale è stata aperta la tastiera
     */
    public static void hideKeyboard(@NonNull Context context, @NonNull View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Show the keyboard if available.
     *
     * @param context current {@link Context}.
     * @param view    {@link View} focused, used to show the keyboard.
     */
    public static void showKeyboard(@NonNull Context context, @NonNull View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Aggiunge lo ScrollingViewBehavior ad una view
     *
     * @param view view alla quale verrà aggiunto il behavior
     */
    public static void attachScrollingBehavior(@NonNull View view) {
        attachBehavior(view, new AppBarLayout.ScrollingViewBehavior());
    }

    /**
     * Aggiunge un Behavior a una View
     *
     * @param view     view alla quale verrà aggiunto il behavior
     * @param behavior behavior da aggiungere
     */
    public static void attachBehavior(@NonNull View view, @Nullable CoordinatorLayout.Behavior behavior) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.LayoutParams coordLayoutParams = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
            coordLayoutParams.setBehavior(behavior);
            view.setLayoutParams(coordLayoutParams);
        }
    }
}