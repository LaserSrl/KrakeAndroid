package com.krake.core;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * ArrayMap con chiavi abbinate a liste di tipi generici.
 * <br/>
 * Vengono già forniti metodi per la serializzazione e la deserializzazione per il passaggio dell'ArrayMap nei Bundle e negli Intent.
 */
public abstract class KeyListMap<T> extends ArrayMap<String, List<T>> {
    private static final String JSON_KEY = "k";
    private static final String JSON_VAL = "v";

    /**
     * Abbina una lista ad una chiave, nel caso in cui la chiave sia già presente, i valori della lista vengono aggiunti alla lista già abbinata a quella chiave.
     *
     * @param key    chiave nell'ArrayMap
     * @param values lista di tipi generici da abbinare alla relativa chiave
     * @return ritorna il vecchio valore abbinato alla chiave oppure null nel caso in cui la chiave non fosse esistita precedentemente
     */
    @Override
    public List<T> put(String key, List<T> values) {
        if (values.size() > 0 && containsKey(key)) {
            values.addAll(get(key));
        }
        return super.put(key, values);
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            List<T> values = get(i);
            if (values != null && values.size() > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Somma la dimensione di tutte le liste per sapere il numero totale di elementi
     *
     * @return dimensione totale degli elementi
     */
    public int getValuesNumber() {
        int size = 0;
        for (int i = 0; i < size(); i++) {
            size += get(keyAt(i)).size();
        }
        return size;
    }

    /**
     * Unisce tutte le liste, a prescindere dalla chiave, in un'unica lista
     *
     * @return lista con tutti gli elementi
     */
    public List<T> getAllValues() {
        List<T> values = new LinkedList<>();
        for (int i = 0; i < size(); i++) {
            List<T> valuesTemp = get(keyAt(i));
            if (valuesTemp != null && valuesTemp.size() > 0) {
                values.addAll(valuesTemp);
            }
        }
        return values;
    }

    /**
     * Serializza la l'ArrayMap creando un json per mantenere gli abbinamenti chiave-valore.
     * L'output sarà:
     * <pre>
     * {@code
     * [
     *     {
     *         k: "key1",
     *         v:["elem1","elem2"]
     *     },
     *     {
     *         k: "key2",
     *         v:["elem3","elem4"]
     *     }
     * ]
     * }
     * </pre>
     *
     * @return stringa con l'ArrayMap serializzato
     */
    public String serialize() {
        JsonArray arrayContainer = new JsonArray();
        for (int i = 0; i < size(); i++) {
            JsonObject objForKey = new JsonObject();
            String key = keyAt(i);
            objForKey.addProperty(JSON_KEY, key);
            List<T> valueList = get(key);
            JsonArray valueArray = new JsonArray();
            for (int j = 0; j < valueList.size(); j++) {
                valueArray.add(typeToJsonElement(valueList.get(j)));
            }
            objForKey.add(JSON_VAL, valueArray);
            arrayContainer.add(objForKey);
        }
        return arrayContainer.toString();
    }

    /**
     * Deserializza il json e ricrea l'ArrayMap valorizzato correttamente
     *
     * @param serialized stringa contenente il json serializzato
     * @return ArrayMap originario
     */
    public KeyListMap<T> deserialize(@NonNull String serialized) {
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = (JsonArray) jsonParser.parse(serialized);

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject currentJsonObj = jsonArray.get(i).getAsJsonObject();
            String key = currentJsonObj.get(JSON_KEY).getAsString();
            JsonArray valueArray = currentJsonObj.get(JSON_VAL).getAsJsonArray();
            int size = valueArray.size();
            List<T> uriList = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                uriList.add(jsonElementToType(valueArray.get(j)));
            }
            put(key, uriList);
        }
        return this;
    }

    /**
     * Metodo usato durante la deserializzazione per implementare dei cambiamenti custom sui singoli elementi.
     *
     * @param element elemento corrente nel json
     * @return elemento del tipo corretto
     */
    protected abstract T jsonElementToType(@NonNull JsonElement element);

    /**
     * Metodo usato durante la serializzazione per implementare dei cambiamenti custom sui singoli elementi.
     *
     * @param typedElement elemento del tipo corretto
     * @return JsonElement corrispondente
     */
    protected abstract JsonElement typeToJsonElement(@NonNull T typedElement);
}