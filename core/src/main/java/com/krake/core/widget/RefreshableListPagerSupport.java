package com.krake.core.widget;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.krake.core.app.ContentItemListMapActivity;
import com.krake.core.data.DataConnectionBase;
import com.krake.core.view.ListViewPagerSupport;

import java.lang.ref.WeakReference;

/**
 * Created by antoniolig on 12/04/16.
 */
public class RefreshableListPagerSupport extends ListViewPagerSupport {
    private WeakReference<ContentItemListMapActivity> activity;

    public RefreshableListPagerSupport(@NonNull DataConnectionBase connection, @NonNull ObjectsRecyclerViewAdapter adapter, ContentItemListMapActivity activity) {
        super(connection, adapter);
        this.activity = new WeakReference<>(activity);
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (activity.get() != null) {

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                int firstPosition = LayoutManagerUtils.findFirstCompletelyVisibleItemPosition((recyclerView).getLayoutManager());
                activity.get().setSwipeRefreshEnabled(firstPosition == 0 || recyclerView.getChildCount() == 0);
            } else if (newState == RecyclerView.SCROLL_STATE_SETTLING || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                activity.get().setSwipeRefreshEnabled(false);
            }
        }
    }
}
