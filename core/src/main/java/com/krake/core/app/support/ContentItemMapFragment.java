package com.krake.core.app.support;

import com.krake.core.OrchardError;
import com.krake.core.app.ContentItemMapModelFragment;
import com.krake.core.app.KrakeApplication;
import com.krake.core.app.OnContentItemSelectedListener;
import com.krake.core.data.DataConnectionModel;
import com.krake.core.data.DataListener;
import com.krake.core.data.DataModel;
import com.krake.core.model.ContentItemWithLocation;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.realm.RealmModel;

/**
 * Classe per mostare su una mappa i contenuti prelevati da orchard
 * I dati devono implementare l'interfaccia {@link ContentItemWithLocation}
 * I dati vengono inseriti sulla mappa sfruttando l'interazione con {@link KrakeApplication#getMarkerCreator()}
 * <p/>
 * Il fragment carica i dati paginati, e procede autonomamente a caricare una pagina dopo l'altra
 * fino a quando non saranno stati scaricati tutti i dati.
 * <p/>
 * <strong>Importante</strong> l'activity che utilizza questo fragment deve implementare l'interfaccia {@link OnContentItemSelectedListener}
 */
@Deprecated
public class ContentItemMapFragment
        extends ContentItemMapModelFragment
        implements DataListener {
    @Override
    public void onDataModelChanged(@org.jetbrains.annotations.Nullable DataModel dataModel) {

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
