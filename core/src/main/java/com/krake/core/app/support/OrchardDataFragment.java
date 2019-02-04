package com.krake.core.app.support;


import com.krake.core.OrchardError;
import com.krake.core.app.OrchardDataModelFragment;
import com.krake.core.data.DataConnectionModel;
import com.krake.core.data.DataListener;
import com.krake.core.data.DataModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
public abstract class OrchardDataFragment extends OrchardDataModelFragment implements DataListener {


    @Override
    public void onDataModelChanged(@Nullable DataModel dataModel) {
        if (dataModel != null)
            onDefaultDataLoaded(dataModel.getListData(), dataModel.getCacheValid());
    }

    @Override
    public void onDataLoadingError(@NotNull OrchardError orchardError) {
        onDefaultDataLoadFailed(orchardError, getDataConnectionModel().getModel().getValue() != null);
    }

    public DataConnectionModel getOrchardConnection() {
        return getDataConnectionModel();
    }

    @Override
    public void onDefaultDataLoadFailed(@NotNull OrchardError error, boolean cachePresent) {

    }
}