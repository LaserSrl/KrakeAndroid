package com.krake.core.app;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.krake.core.OrchardError;
import com.krake.core.data.DataModel;

/**
 * Created by joel on 17/12/15.
 */
public interface DataConnectionLoadListener {

    void onDataLoadFailed(@NonNull OrchardError error, @Nullable DataModel dataModel);

    void onDataLoading(boolean isLoading, int page);
}
