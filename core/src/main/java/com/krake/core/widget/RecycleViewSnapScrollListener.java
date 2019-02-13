package com.krake.core.widget;

import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by joel on 06/08/15.
 */
public class RecycleViewSnapScrollListener extends RecyclerView.OnScrollListener {


    //To avoid recursive calls
    private boolean mAutoSet = true;

    //The pivot to be snapped to
    private int mCenterPivot;


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();

        int layoutOrientation = LayoutManagerUtils.getOrientation(lm);
        if (mCenterPivot == 0) {

            // Default pivot , Its a bit inaccurate .
            // Better pass the center pivot as your Center Indicator view's
            // calculated center on it OnGlobalLayoutListener event
            mCenterPivot = layoutOrientation == LinearLayoutManager.HORIZONTAL ?
                    (recyclerView.getLeft() + recyclerView.getRight()) / 2 : (recyclerView.getTop() + recyclerView.getBottom()) / 2;
        }
        if (!mAutoSet) {

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                //ScrollStoppped
                int centerPosition = findCenterPosition(lm);
                View view = lm.findViewByPosition(centerPosition);//get the view nearest to center
                if(view != null) {
                    int viewCenter = layoutOrientation == LinearLayoutManager.HORIZONTAL ? (view.getLeft() + view.getRight()) / 2 : (view.getTop() + view.getBottom()) / 2;
                    //compute scroll from center
                    int scrollNeeded = viewCenter - mCenterPivot; // Add or subtract any offsets you need here

                    if (layoutOrientation == LinearLayoutManager.HORIZONTAL) {
                        recyclerView.smoothScrollBy(scrollNeeded, 0);

                    } else {
                        recyclerView.smoothScrollBy(0, -scrollNeeded);
                    }
                }
                mAutoSet = true;
            }
        }
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {

            mAutoSet = false;
        }
    }

    private int findCenterPosition(RecyclerView.LayoutManager lm) {

        int minDistance = 0;
        View view;
        int returnPosition = 0;
        boolean notFound = true;
        int itemCount = lm.getItemCount();

        int layoutOrientation = LayoutManagerUtils.getOrientation(lm);
        int firstVisible = LayoutManagerUtils.findFirstCompletelyVisibleItemPosition(lm);
        int lastCompletelyVisible = LayoutManagerUtils.findLastCompletelyVisibleItemPosition(lm);

        if (itemCount > 1) {
            if(lastCompletelyVisible == itemCount - 1) {
                return lastCompletelyVisible;
            }

            if (firstVisible == 0) {
                return firstVisible;
            }
        }

        if(firstVisible == -1)
            firstVisible = LayoutManagerUtils.findFirstVisibleItemPosition(lm);
        int lastVisible = LayoutManagerUtils.findLastVisibleItemPosition(lm);



        for (int i = firstVisible; i <= lastVisible && notFound; i++) {
            view = lm.findViewByPosition(i);
            if (view != null) {
                int center = layoutOrientation == LinearLayoutManager.HORIZONTAL ? (view.getLeft() + view.getRight()) / 2 : (view.getTop() + view.getBottom()) / 2;
                int leastDifference = Math.abs(mCenterPivot - center);

                if (leastDifference <= minDistance || i == firstVisible) {
                    minDistance = leastDifference;
                    returnPosition = i;
                } else {
                    notFound = false;

                }
            }
        }
        return returnPosition;
    }
}

