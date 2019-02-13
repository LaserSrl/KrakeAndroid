package com.krake.contentcreation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Definizione di un nuovo contenuto da creare in Orchard.
 */
public class ContentDefinition {
    private final String contentType;
    private final boolean saveInfos;
    private final List<ContentCreationTabInfo> tabs;

    private final JsonObject contentAdditionalNonEditableInfos;

    /**
     * @param contentType                       nome del content type in Orchard
     * @param tabs                              elenco dei tab da creare il contenuto
     * @param contentAdditionalNonEditableInfos dati aggiuntivi della
     */
    public ContentDefinition(@NonNull String contentType, boolean saveInfos, @NonNull List<ContentCreationTabInfo> tabs, @Nullable JsonObject contentAdditionalNonEditableInfos) {
        this.contentType = contentType;
        this.saveInfos = saveInfos;
        this.tabs = tabs;
        this.contentAdditionalNonEditableInfos = contentAdditionalNonEditableInfos;
    }

    public boolean getSaveInfos() {
        return saveInfos;
    }

    public String getContentType() {
        return contentType;
    }

    public List<ContentCreationTabInfo> getTabs() {
        return tabs;
    }

    public JsonObject getContentAdditionalNonEditableInfos() {
        return contentAdditionalNonEditableInfos;
    }
}
