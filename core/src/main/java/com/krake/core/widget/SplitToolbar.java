package com.krake.core.widget;

import android.content.Context;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by joel on 20/01/15.
 */
public class SplitToolbar extends Toolbar {
    public SplitToolbar(Context context) {
        super(context);
        setContentInsetsAbsolute(0, 0);
        setContentInsetsRelative(0, 0);
    }

    public SplitToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setContentInsetsAbsolute(0, 0);
        setContentInsetsRelative(0, 0);
    }

    public SplitToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setContentInsetsAbsolute(0, 0);
        setContentInsetsRelative(0, 0);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (child instanceof ActionMenuView) {
            params.width = LayoutParams.MATCH_PARENT;
        }
        super.addView(child, params);
    }
}