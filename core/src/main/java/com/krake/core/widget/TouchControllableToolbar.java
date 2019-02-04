package com.krake.core.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Toolbar che permette il controllo sulla gestione dei touch.
 * <br/>
 * La Toolbar di default di appcompat-v7 non si cura dell'attributo <i>clickable</i> quindi il metodo setClickable(boolean) risulta inutile.
 */
public class TouchControllableToolbar extends Toolbar {
    private boolean mEatingTouch = true;

    public TouchControllableToolbar(Context context) {
        super(context);
    }

    public TouchControllableToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchControllableToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // il controllo viene fatto a ogni touch
        // il metodo di controllo della Toolbar viene utilizzato solo nel caso in cui il touch debba fermarsi alla Toolbar
        return mEatingTouch && super.onTouchEvent(ev);
    }

    /**
     * Controlla se il touch passa alle view sottostanti o meno
     *
     * @return false se il touch passa alle view sottostanti
     */
    public boolean isEatingTouchGestures() {
        return mEatingTouch;
    }

    /**
     * Permette di stabilire se la Toolbar debba lasciar passare o meno il touch event alle view sottostanti
     *
     * @param eatTouch false nel caso in cui si voglia far passare il touch
     */
    public void eatTouchGestures(boolean eatTouch) {
        mEatingTouch = eatTouch;
    }
}