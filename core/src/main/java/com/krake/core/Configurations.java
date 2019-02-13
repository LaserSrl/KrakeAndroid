package com.krake.core;

import android.content.Context;
import androidx.collection.ArrayMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.krake.core.util.StringWithRealmNameKt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Configurazioni del mapper, le impostazioni di default sono caricate da file R.raw.mapper_configurations
 * Created by joel on 23/07/14.
 */
public class Configurations {
    private List<String> garbageParts;

    private ArrayMap<String, String> specialNamesMapping;

    private List<Pattern> multipleValuesKeyRegex;

    private ArrayMap<String, List<String>> mGeneratedClasses;

    /**
     * Language per la chiamata al WS.
     * Default: identifier della locale corrente
     */
    private String languageIdentifier;

    protected Configurations(Context context) {
        languageIdentifier = context.getString(R.string.orchard_language);

        try {
            JsonObject json = loadJsonObjectFromRaw(context, R.raw.mapper_configurations);

            JsonArray readGarbages = json.getAsJsonArray("GarbageParts");

            JsonArray readRejex = json.getAsJsonArray("MultipleValuesKeyRegex");
            JsonObject readSpecialNamesMapping = json.getAsJsonObject("SpecialNamesMapping");

            mGeneratedClasses = new ArrayMap<>();

            JsonObject generatedClassesInfos = loadJsonObjectFromRaw(context, R.raw.generated_classes_support);

            for (Map.Entry<String, JsonElement> entry : generatedClassesInfos.entrySet()) {
                List<String> fields = new LinkedList<>();

                JsonArray jsonFields = entry.getValue().getAsJsonArray();

                for (int i = 0; i < jsonFields.size(); ++i) {
                    fields.add(jsonFields.get(i).getAsString());
                }

                mGeneratedClasses.put(entry.getKey(), fields);
            }

            garbageParts = new ArrayList<>();
            for (int i = 0; i < readGarbages.size(); ++i) {
                garbageParts.add(readGarbages.get(i).getAsString());
            }

            multipleValuesKeyRegex = new ArrayList<>();
            for (int i = 0; i < readRejex.size(); ++i) {
                multipleValuesKeyRegex.add(Pattern.compile(readRejex.get(i).getAsString()));
            }

            specialNamesMapping = new ArrayMap<>();

            for (Map.Entry<String, JsonElement> entry : readSpecialNamesMapping.entrySet()) {
                specialNamesMapping.put(entry.getKey(), entry.getValue().getAsString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            //throw e;
        }
    }

    private JsonObject loadJsonObjectFromRaw(Context context, int rawConfigurationIdentifier) throws IOException {
        InputStream input = context.getResources().openRawResource(rawConfigurationIdentifier);

        int byteRead;
        byte[] bytes = new byte[4096];

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        do {

            byteRead = input.read(bytes);

            if (byteRead > 0)
                buffer.write(bytes, 0, byteRead);

        } while (byteRead != -1);

        return new Gson().fromJson(new String(buffer.toByteArray(), "UTF-8"), JsonObject.class);
    }

    public List<String> getGarbageParts() {
        return garbageParts;
    }

    public Map<String, String> getSpecialNamesMapping() {
        return specialNamesMapping;
    }

    public List<Pattern> getMultipleValuesKeyRegex() {
        return multipleValuesKeyRegex;
    }

    public String getLanguageIdentifier() {
        return languageIdentifier;
    }

    public boolean getContainsGeneratedClass(String className) {
        return mGeneratedClasses.containsKey(StringWithRealmNameKt.realmCleanClassName(className));
    }

    public List<String> getGeneratedClassFields(String className) {
        return mGeneratedClasses.get(StringWithRealmNameKt.realmCleanClassName(className));
    }
}
