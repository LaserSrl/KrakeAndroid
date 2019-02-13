package com.krake.core.widget;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * Created by joel on 02/03/16.
 */
public class LayoutManagerUtils {
    public static int findFirstCompletelyVisibleItemPosition(RecyclerView.LayoutManager manager) {
        if (manager instanceof LayoutManagerWithFirstVisiblePosition) {
            return ((LayoutManagerWithFirstVisiblePosition) manager).findFirstCompletelyVisibleItemPosition();
        } else if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).findFirstCompletelyVisibleItemPosition();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) manager).findFirstCompletelyVisibleItemPositions(null)[0];
        }
        return -1;
    }

    public static int findFirstVisibleItemPosition(RecyclerView.LayoutManager manager) {
        if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).findFirstVisibleItemPosition();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) manager).findFirstVisibleItemPositions(null)[0];
        }
        return -1;
    }

    public static int findLastVisibleItemPosition(RecyclerView.LayoutManager manager) {
        if (manager instanceof LayoutManagerWithFirstVisiblePosition) {
            return ((LayoutManagerWithFirstVisiblePosition) manager).findLastVisibleItemPosition();
        } else if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).findLastVisibleItemPosition();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) manager).findLastVisibleItemPositions(null)[0];
        }
        return -1;
    }

    public static int findLastCompletelyVisibleItemPosition(RecyclerView.LayoutManager manager) {
        if (manager instanceof LayoutManagerWithFirstVisiblePosition) {
            return ((LayoutManagerWithFirstVisiblePosition) manager).findLastVisibleItemPosition();
        } else if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).findLastCompletelyVisibleItemPosition();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) manager).findLastCompletelyVisibleItemPositions(null)[0];
        }
        return -1;
    }

    public static int getOrientation(RecyclerView.LayoutManager manager) {
        if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).getOrientation();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) manager).getOrientation();
        }
        return 0;
    }
}
