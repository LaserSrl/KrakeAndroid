package com.krake.core.content;

/**
 * Created by joel on 02/10/14.
 */
public interface UpdatebleOrchardDataLoader {
    void updateDisplayPath(String displayPath, boolean reloadImmediately);

    void setExtraParameter(String key, String value, boolean reloadImmediately);
}
