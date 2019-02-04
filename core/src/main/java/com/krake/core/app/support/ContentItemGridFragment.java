package com.krake.core.app.support;

import com.krake.core.OrchardError;
import com.krake.core.app.ContentItemGridModelFragment;
import com.krake.core.content.UpdatebleOrchardDataLoader;
import com.krake.core.data.DataConnectionModel;
import com.krake.core.data.DataListener;
import com.krake.core.data.DataModel;
import com.krake.core.model.ContentItem;
import com.krake.core.widget.ObjectsRecyclerViewAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import io.realm.RealmModel;

@Deprecated
public class ContentItemGridFragment
        extends ContentItemGridModelFragment
        implements UpdatebleOrchardDataLoader,
        ObjectsRecyclerViewAdapter.ClickReceiver<ContentItem>,
        DataListener {
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

    public void onDefaultDataLoadFailed(@NotNull OrchardError error, boolean cachePresent) {

    }

    @Override
    public void onDefaultDataLoaded(@NotNull List<? extends RealmModel> list, boolean cacheValid) {
        super.onDataModelChanged(new DataModel(list, cacheValid));
    }
}