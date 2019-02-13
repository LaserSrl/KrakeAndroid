package com.krake.core.app;

import androidx.annotation.NonNull;
import com.krake.core.model.ContentItem;

/**
 * Interfaccia che devono implementare le {@link android.app.Activity} che utilizzano i fragment
 * {@link ContentItemGridModelFragment} {@link ContentItemMapModelFragment}.
 */
public interface OnContentItemSelectedListener {
    /**
     * Notifica che è necessario mostrare il dettaglio relativo ad un certo oggetto
     *
     * @param senderFragment fragment su cui è stato selezionato il dettaglio
     * @param contentItem    oggeto di cui mostrare il dettaglio
     */
    void onShowContentItemDetails(@NonNull Object senderFragment, @NonNull ContentItem contentItem);

    /**
     * Notifica che è selezionato e messo in evidenza un oggetto.
     * Questo metodo viene utilizzato ad esempio da {@link ContentItemListMapActivity} per mostrare
     * anche su mappa un contenuto che è stato selezionato su lista
     *
     * @param senderFragment fragment su cui è stato selezionato il dettaglio
     * @param contentItem    oggeto di cui mostrare il dettaglio
     */
    void onContentItemInEvidence(@NonNull Object senderFragment, @NonNull ContentItem contentItem);
}
