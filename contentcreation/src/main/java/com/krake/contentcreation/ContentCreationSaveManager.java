package com.krake.contentcreation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by joel on 16/12/15.
 */
final class ContentCreationSaveManager {
    private static final String CONTENT_CREATION_PREFS_NAME = "ContentCreation";

    private ContentCreationSaveManager() {
        // empty private constructor
    }

    @SuppressLint("CommitPrefEdits")
    static void saveContentCreationInfos(@NonNull Context context, @NonNull String contentType, @Nullable ArrayList<String> savedContentBundle) {
        SharedPreferences.Editor editor = context.getSharedPreferences(CONTENT_CREATION_PREFS_NAME, Context.MODE_PRIVATE).edit();
        if (savedContentBundle == null)
            editor.remove(contentType);
        else
            editor.putString(contentType, ContentCreationUtils.getGsonInstance().toJson(savedContentBundle));

        editor.commit();
    }

    @Nullable
    static ArrayList<String> loadContentCreationBundle(@NonNull Context context, @NonNull String contentType) {
        SharedPreferences prefs = context.getSharedPreferences(CONTENT_CREATION_PREFS_NAME, Context.MODE_PRIVATE);
        String savedValue = prefs.getString(contentType, null);
        if (savedValue != null)
            return ContentCreationUtils.getGsonInstance().fromJson(savedValue, new TypeToken<ArrayList<String>>() {
            }.getType());

        return null;
    }
}