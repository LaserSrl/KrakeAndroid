package com.krake.core.view;

import androidx.annotation.NonNull;
import com.krake.core.data.DataConnectionBase;
import com.krake.core.model.ContentItemWithLocation;

import java.util.List;

/**
 * Supporto per caricare contenuti paginata in una mappa.
 * Le pagine vengono caricate una dopo l'altra fino a quando non si sono caricati tutti i contenuti.
 * La classe interagisce autonomaente con la connection in modo da scaricare tutti i dati necessari.
 * Created by joel on 14/11/14.
 */
public class MapViewPagerSupport {
    private DataConnectionBase mConnection;

    public MapViewPagerSupport(DataConnectionBase connection) {
        mConnection = connection;
    }

    public void onDataLoaded(@NonNull List<ContentItemWithLocation> items) {
        int pageSize = mConnection.getOrchardModule().getPageSize();
        if (pageSize > 0 &&
                !mConnection.isLoadingData() &&
                items.size() == mConnection.getPage() * pageSize &&
                items.size() > 0) {

            mConnection.setPage(mConnection.getPage() + 1);
            mConnection.loadDataFromRemote();
        }
    }
}
