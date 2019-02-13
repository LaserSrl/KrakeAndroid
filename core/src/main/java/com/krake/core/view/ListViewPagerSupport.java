package com.krake.core.view;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.krake.core.data.DataConnectionBase;
import com.krake.core.widget.LayoutManagerUtils;
import com.krake.core.widget.ObjectsRecyclerViewAdapter;

/**
 * Classe per paginare una lista,
 * viene caricata la pagina successiva quando scrorrendo la lista ci sono meno di 3 elementi rimanenti ancora
 * da mostrare.
 * IL pager integragisce direttamente con la connection andando ad avviare nuove connessioni.
 */
public class ListViewPagerSupport extends RecyclerView.OnScrollListener {
    private DataConnectionBase mConnection;
    private ObjectsRecyclerViewAdapter mAdapter;

    public ListViewPagerSupport(@NonNull DataConnectionBase connection, @NonNull ObjectsRecyclerViewAdapter adapter) {
        mConnection = connection;
        mAdapter = adapter;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();

        int pageSize = mConnection.getOrchardModule().getPageSize();
        if (pageSize > 0 &&
                !mConnection.isLoadingData() &&
                mAdapter.getItemCount() == mConnection.getPage() * pageSize &&
                mAdapter.getItemCount() > 0) {

                if (LayoutManagerUtils.findLastVisibleItemPosition(manager) >= mAdapter.getItemCount() - 3) {
                    mConnection.setPage(mConnection.getPage() + 1);
                    mConnection.loadDataFromRemote();
                }
            }

    }
}
